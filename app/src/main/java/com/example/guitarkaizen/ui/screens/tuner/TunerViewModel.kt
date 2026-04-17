package com.example.guitarkaizen.ui.screens.tuner

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.lifecycle.ViewModel
import com.example.guitarkaizen.data.Tuning
import com.example.guitarkaizen.data.TuningDictionary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.pitch.PitchDetectionHandler
import be.tarsos.dsp.pitch.PitchProcessor
import be.tarsos.dsp.pitch.PitchProcessor.PitchEstimationAlgorithm
import be.tarsos.dsp.io.TarsosDSPAudioFormat
import be.tarsos.dsp.io.TarsosDSPAudioInputStream
import kotlin.math.log2

data class TunerState(
    val currentPitchHz: Float = 0.0f,
    val closestNoteName: String = "-",
    val targetPitchHz: Float = 0.0f,
    val centsOff: Float = 0.0f,
    val isAutoMode: Boolean = true,
    val selectedTuning: Tuning = TuningDictionary.tunings.first(),
    val selectedStringIndex: Int = 5 // Default to String 6 (index 5)
)

class TunerViewModel : ViewModel() {

    private val _state = MutableStateFlow(TunerState())
    val state: StateFlow<TunerState> = _state.asStateFlow()

    private var audioThread: Thread? = null
    private var dispatcher: AudioDispatcher? = null
    private var currentRecorder: AudioRecord? = null
    private var smoothedPitchHz = 0.0f
    private var invalidFrameCount = 0

    @SuppressLint("MissingPermission")
    fun startListening() {
        stopListening()
        try {
            val sampleRate = 44100
            val bufferSize = 2048
            val overlap = 1024

            val minBufferSize = AudioRecord.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            val recorderSize = maxOf(bufferSize * 2, minBufferSize)
            val recorder = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                recorderSize
            )

            if (recorder.state != AudioRecord.STATE_INITIALIZED) {
                return
            }

            currentRecorder = recorder
            recorder.startRecording()

            // Custom inline TarsosDSP stream to bridge Android microphone capture natively
            val format = TarsosDSPAudioFormat(
                sampleRate.toFloat(),
                16, // sample size in bits
                1,  // channels
                true, // signed
                false // big endian (Android is little endian)
            )

            val audioStream = object : TarsosDSPAudioInputStream {
                override fun getFormat(): TarsosDSPAudioFormat = format
                
                override fun read(b: ByteArray, off: Int, len: Int): Int {
                    val rec = currentRecorder ?: return -1
                    val readBytes = rec.read(b, off, len)
                    return if (readBytes < 0) -1 else readBytes
                }
                
                override fun skip(n: Long): Long = 0
                
                override fun close() {
                    stopListening()
                }
                
                override fun getFrameLength(): Long = -1
            }

            val disp = AudioDispatcher(audioStream, bufferSize, overlap)
            dispatcher = disp

            val pdh = PitchDetectionHandler { result, _ ->
                val pitch = result.pitch
                if (pitch > 0 && result.probability >= 0.85f) {
                    invalidFrameCount = 0
                    processIncomingPitch(pitch)
                } else {
                    invalidFrameCount++
                    if (invalidFrameCount >= 5) {
                        clearPitchSignal()
                    }
                }
            }

            val pitchProcessor = PitchProcessor(
                PitchEstimationAlgorithm.YIN,
                sampleRate.toFloat(),
                bufferSize,
                pdh
            )
            disp.addAudioProcessor(pitchProcessor)

            val thread = Thread(disp, "Tuner Audio Dispatcher Thread")
            audioThread = thread
            thread.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopListening() {
        smoothedPitchHz = 0.0f
        invalidFrameCount = 0
        try {
            dispatcher?.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        dispatcher = null

        try {
            currentRecorder?.stop()
            currentRecorder?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        currentRecorder = null

        audioThread?.interrupt()
        audioThread = null
    }

    private fun clearPitchSignal() {
        smoothedPitchHz = 0.0f
        _state.update {
            it.copy(
                currentPitchHz = 0.0f,
                centsOff = 0.0f
            )
        }
    }

    private fun processIncomingPitch(pitch: Float) {
        val currentState = _state.value
        val selectedTuning = currentState.selectedTuning

        // 1. Exponential Moving Average (EMA) smoothing
        if (smoothedPitchHz == 0.0f) {
            smoothedPitchHz = pitch
        } else {
            smoothedPitchHz = (pitch * 0.15f) + (smoothedPitchHz * 0.85f)
        }
        val currentSmoothed = smoothedPitchHz

        val targetHz: Float
        val targetName: String

        // 2. The Chromatic Engine vs. standard 6-string dictionary logic
        if (selectedTuning.id == "chromatic") {
            // Convert smoothed Hz to MIDI note: round(69 + 12 * log2(hz / 440.0))
            val midiNote = kotlin.math.round(69.0 + 12.0 * log2((currentSmoothed / 440.0f).toDouble())).toInt()
            val clampedMidi = midiNote.coerceIn(24, 108) // Standard range E1 (24) to C8 (108)
            targetHz = (440.0 * java.lang.Math.pow(2.0, (clampedMidi - 69) / 12.0)).toFloat()

            val noteNames = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
            val pitchClass = clampedMidi % 12
            val octave = (clampedMidi / 12) - 1
            targetName = "${noteNames[pitchClass]}$octave"
        } else {
            // Determine target string based on Auto vs Manual mode
            val targetString = if (currentState.isAutoMode) {
                selectedTuning.strings.minByOrNull { kotlin.math.abs(it.frequencyHz - currentSmoothed) }
                    ?: selectedTuning.strings.first()
            } else {
                selectedTuning.strings.firstOrNull { it.index == currentState.selectedStringIndex }
                    ?: selectedTuning.strings.first()
            }
            targetHz = targetString.frequencyHz.toFloat()
            targetName = targetString.name
        }

        // Exact cents off calculation: 1200 * log2(currentSmoothed / targetHz)
        val cents = if (currentSmoothed > 0 && targetHz > 0) {
            (1200.0 * log2((currentSmoothed / targetHz).toDouble())).toFloat()
        } else {
            0.0f
        }

        _state.update {
            it.copy(
                currentPitchHz = currentSmoothed,
                closestNoteName = targetName,
                targetPitchHz = targetHz,
                centsOff = cents
            )
        }
    }

    fun setAutoMode(enabled: Boolean) {
        _state.update { it.copy(isAutoMode = enabled) }
        triggerPitchUpdate()
    }

    fun setSelectedTuning(tuning: Tuning) {
        _state.update { it.copy(selectedTuning = tuning) }
        triggerPitchUpdate()
    }

    fun setSelectedStringIndex(index: Int) {
        _state.update { it.copy(selectedStringIndex = index) }
        triggerPitchUpdate()
    }

    private fun triggerPitchUpdate() {
        val pitch = _state.value.currentPitchHz
        if (pitch > 0) {
            processIncomingPitch(pitch)
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopListening()
    }
}
