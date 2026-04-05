package com.example.guitarkaizen.ui.screens.eartraining

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import kotlinx.coroutines.delay

class EarTrainerAudioEngine {

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(6)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    // Chromatic note names for 2 octaves (C3 to B4)
    private val notesList = listOf(
        "C3", "C#3", "D3", "D#3", "E3", "F3", "F#3", "G3", "G#3", "A3", "A#3", "B3",
        "C4", "C#4", "D4", "D#4", "E4", "F4", "F#4", "G4", "G#4", "A4", "A#4", "B4"
    )

    private val loadedSoundIds = mutableMapOf<String, Int>()

    // load samples from res/raw
    fun load(context: Context) {
        notesList.forEach { noteName ->
            val resName = "note_" + noteName.lowercase().replace("#", "s")
            val resId = context.resources.getIdentifier(resName, "raw", context.packageName)
            if (resId != 0) {
                try {
                    val soundId = soundPool.load(context, resId, 1)
                    loadedSoundIds[noteName] = soundId
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun getNoteNameFromMidi(midi: Int): String {
        val noteNames = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
        val pc = midi % 12
        val octave = (midi / 12) - 1
        return "${noteNames[pc]}$octave"
    }

    // play MIDI pitch, fallback to pitch-shifting if outside C3-B4
    fun playNote(absoluteMidi: Int, volume: Float = 1.0f) {
        val baseMidi: Int
        val soundId: Int?
        
        if (absoluteMidi < 48) {
            baseMidi = 48
            soundId = loadedSoundIds["C3"]
        } else if (absoluteMidi > 71) {
            baseMidi = 71
            soundId = loadedSoundIds["B4"]
        } else {
            baseMidi = absoluteMidi
            val noteName = getNoteNameFromMidi(absoluteMidi)
            soundId = loadedSoundIds[noteName]
        }

        if (soundId != null && soundId != 0) {
            val semitoneDiff = absoluteMidi - baseMidi
            val rate = Math.pow(2.0, semitoneDiff.toDouble() / 12.0).toFloat().coerceIn(0.5f, 2.0f)
            try {
                soundPool.play(soundId, volume, volume, 1, 0, rate)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // play wrapper
    fun playPitch(absoluteMidi: Int, volume: Float = 1.0f) {
        playNote(absoluteMidi, volume)
    }

    // play note by name
    fun playNote(noteName: String, volume: Float = 1.0f) {
        val soundId = loadedSoundIds[noteName]
        if (soundId != null && soundId != 0) {
            try {
                soundPool.play(soundId, volume, volume, 1, 0, 1f)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // play chord by name
    fun playChord(noteNames: List<String>, volume: Float = 1.0f) {
        noteNames.forEach { noteName ->
            playNote(noteName, volume)
        }
    }

    // play chord by MIDI
    fun playChordMidi(absoluteMidis: List<Int>, volume: Float = 1.0f) {
        absoluteMidis.forEach { midi ->
            playNote(midi, volume)
        }
    }

    // strum chord by name
    suspend fun strumChord(noteNames: List<String>, delayMs: Long = 40L) {
        noteNames.forEachIndexed { index, noteName ->
            playNote(noteName)
            if (index < noteNames.lastIndex) {
                delay(delayMs)
            }
        }
    }

    // strum chord by MIDI
    suspend fun strumChordMidi(absoluteMidis: List<Int>, delayMs: Long = 40L) {
        absoluteMidis.forEachIndexed { index, midi ->
            playNote(midi)
            if (index < absoluteMidis.lastIndex) {
                delay(delayMs)
            }
        }
    }

    // release sound resources
    fun release() {
        try {
            soundPool.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        loadedSoundIds.clear()
    }
}
