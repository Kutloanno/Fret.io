package com.example.guitarkaizen.ui.screens.eartraining

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

data class FunctionalEarState(
    val currentKey: String = "C",
    val targetScaleDegree: Int = 1,
    val score: Int = 0,
    val isPlayingCadence: Boolean = false
)

class FunctionalEarViewModel : ViewModel() {

    private val _state = MutableStateFlow(FunctionalEarState())
    val state: StateFlow<FunctionalEarState> = _state.asStateFlow()

    private val engine = EarTrainerAudioEngine()
    private var isEngineLoaded = false

    private val keyMidiMap = mapOf(
        "C" to 48, "C#" to 49, "D" to 50, "D#" to 51, "E" to 52, "F" to 53,
        "F#" to 54, "G" to 55, "G#" to 56, "A" to 57, "A#" to 58, "B" to 59
    )

    private val noteNames = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")

    init {
        generateNewTarget()
    }

    /**
     * Initializes the sampler audio engine safely under Context reference.
     */
    fun initEngine(context: Context) {
        if (!isEngineLoaded) {
            engine.load(context)
            isEngineLoaded = true
        }
    }

    /**
     * Generates a new random scale degree (1 to 7) for testing.
     */
    fun generateNewTarget() {
        val nextDegree = (1..7).random()
        _state.update { it.copy(targetScaleDegree = nextDegree) }
    }

    /**
     * Updates testing key locks and automatically regenerates and triggers cadence play.
     */
    fun setKey(key: String) {
        if (keyMidiMap.containsKey(key)) {
            _state.update { it.copy(currentKey = key) }
            generateNewTarget()
            playCadence()
        }
    }

    /**
     * Evaluates the guessed scale degree.
     * Increments scores on success.
     */
    fun submitGuess(guessDegree: Int): Boolean {
        val currentState = _state.value
        val isCorrect = guessDegree == currentState.targetScaleDegree
        if (isCorrect) {
            _state.update { it.copy(score = currentState.score + 1) }
            generateNewTarget()
        }
        return isCorrect
    }

    /**
     * Plays the I-IV-V-I cadence dynamically transposed to currentKey.
     * Follows delay specifications (850ms chord intervals, 500ms target note delay).
     */
    fun playCadence() {
        val currentState = _state.value
        if (currentState.isPlayingCadence) return

        _state.update { it.copy(isPlayingCadence = true) }

        viewModelScope.launch {
            try {
                val key = currentState.currentKey
                val baseMidi = keyMidiMap[key] ?: 48 // Default C3

                // Triads semitone offsets in root position:
                // I triad: 0, 4, 7 (Root, Maj 3rd, Perf 5th)
                // IV triad: 5, 9, 12 (Perf 4th, Maj 6th, Octave)
                // V triad: 7, 11, 14 (Perf 5th, Maj 7th, Maj 2nd octave)
                val chordIOffsets = listOf(0, 4, 7)
                val chordIVOffsets = listOf(5, 9, 12)
                val chordVOffsets = listOf(7, 11, 14)

                val chordI = chordIOffsets.map { getNoteName(baseMidi + it) }
                val chordIV = chordIVOffsets.map { getNoteName(baseMidi + it) }
                val chordV = chordVOffsets.map { getNoteName(baseMidi + it) }

                // Play Cadence Sequence: I -> IV -> V -> I
                engine.playChord(chordI)
                delay(850)

                engine.playChord(chordIV)
                delay(850)

                engine.playChord(chordV)
                delay(850)

                engine.playChord(chordI)
                delay(850)

                // Wait 500ms before playing target note
                delay(500)

                // Scale degrees offsets:
                // 1 -> Root (0), 2 -> M2 (2), 3 -> M3 (4), 4 -> P4 (5), 5 -> P5 (7), 6 -> M6 (9), 7 -> M7 (11)
                val degreeOffsets = intArrayOf(0, 2, 4, 5, 7, 9, 11)
                val targetDegree = currentState.targetScaleDegree
                val offset = if (targetDegree in 1..7) degreeOffsets[targetDegree - 1] else 0
                val targetNote = getNoteName(baseMidi + offset)

                engine.playNote(targetNote)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _state.update { it.copy(isPlayingCadence = false) }
            }
        }
    }

    /**
     * Replays the target note independently.
     */
    fun replayTargetNote() {
        val currentState = _state.value
        val baseMidi = keyMidiMap[currentState.currentKey] ?: 48
        val degreeOffsets = intArrayOf(0, 2, 4, 5, 7, 9, 11)
        val targetDegree = currentState.targetScaleDegree
        val offset = if (targetDegree in 1..7) degreeOffsets[targetDegree - 1] else 0
        val targetNote = getNoteName(baseMidi + offset)
        engine.playNote(targetNote)
    }

    /**
     * Resets ear trainer scores.
     */
    fun resetScore() {
        _state.update { it.copy(score = 0) }
        generateNewTarget()
    }

    /**
     * Helper to fold MIDI note numbers cleanly into standard sampler range C3 (48) to B4 (71)
     */
    private fun getNoteName(midi: Int): String {
        var m = midi
        while (m > 71) {
            m -= 12
        }
        while (m < 48) {
            m += 12
        }
        val pitchClass = m % 12
        val octave = (m / 12) - 1
        return "${noteNames[pitchClass]}$octave"
    }

    override fun onCleared() {
        super.onCleared()
        engine.release()
    }
}
