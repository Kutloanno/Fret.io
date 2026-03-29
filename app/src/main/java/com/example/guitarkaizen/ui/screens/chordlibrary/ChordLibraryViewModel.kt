package com.example.guitarkaizen.ui.screens.chordlibrary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.guitarkaizen.data.ChordDictionary
import com.example.guitarkaizen.data.ChordVoicing
import com.example.guitarkaizen.ui.screens.eartraining.EarTrainerAudioEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// state for chord library screen
data class ChordLibraryState(
    val selectedRoot: String = "C",
    val selectedQuality: String = "Major",
    val currentVoicingIndex: Int = 0,
    val isLeftHanded: Boolean = false,
    val isValidSelection: Boolean = true
)

class ChordLibraryViewModel : ViewModel() {

    private val _state = MutableStateFlow(ChordLibraryState())
    val state: StateFlow<ChordLibraryState> = _state.asStateFlow()

    private val stringBases = listOf(40, 45, 50, 55, 59, 64) // E2, A2, D3, G3, B3, E4 in standard tuning MIDI
    private val noteNames = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")

    init {
        val initial = _state.value
        val voicings = ChordDictionary.getVoicings(initial.selectedRoot, initial.selectedQuality)
        _state.update { it.copy(isValidSelection = voicings.isNotEmpty()) }
    }

    fun setRoot(root: String) {
        _state.update {
            val newState = it.copy(
                selectedRoot = root,
                currentVoicingIndex = 0
            )
            val voicings = ChordDictionary.getVoicings(newState.selectedRoot, newState.selectedQuality)
            newState.copy(isValidSelection = voicings.isNotEmpty())
        }
    }

    fun setQuality(quality: String) {
        _state.update {
            val newState = it.copy(
                selectedQuality = quality,
                currentVoicingIndex = 0
            )
            val voicings = ChordDictionary.getVoicings(newState.selectedRoot, newState.selectedQuality)
            newState.copy(isValidSelection = voicings.isNotEmpty())
        }
    }

    fun setVoicingIndex(index: Int) {
        val total = getVoicingsCount()
        if (total > 0) {
            val safeIndex = index.coerceIn(0, total - 1)
            _state.update { it.copy(currentVoicingIndex = safeIndex) }
        }
    }

    fun toggleLeftHanded() {
        _state.update { it.copy(isLeftHanded = !it.isLeftHanded) }
    }

    // filter dictionary by root & quality
    fun getFilteredVoicings(): List<ChordVoicing> {
        val currentState = _state.value
        return ChordDictionary.getVoicings(currentState.selectedRoot, currentState.selectedQuality)
    }

    // get active voicing variation
    fun getCurrentVoicing(): ChordVoicing? {
        val voicings = getFilteredVoicings()
        if (voicings.isEmpty()) return null
        val idx = _state.value.currentVoicingIndex.coerceIn(0, voicings.lastIndex)
        return voicings[idx]
    }

    // variations count
    fun getVoicingsCount(): Int {
        return getFilteredVoicings().size
    }

    // convert chord to piano notes, clamp range (C3-B4), then strum
    fun strumCurrentVoicing(engine: EarTrainerAudioEngine) {
        val voicing = getCurrentVoicing() ?: return
        val strumNotes = mutableListOf<String>()

        voicing.frets.forEachIndexed { stringIdx, fret ->
            if (fret >= 0) {
                val baseMidi = stringBases[stringIdx]
                val midi = baseMidi + fret

                // Apply standard octave folding to fold note cleanly into C3 (48) to B4 (71) bounds
                var m = midi
                while (m > 71) {
                    m -= 12
                }
                while (m < 48) {
                    m += 12
                }

                val pitchClass = m % 12
                val octave = (m / 12) - 1
                val noteName = "${noteNames[pitchClass]}$octave"
                strumNotes.add(noteName)
            }
        }

        if (strumNotes.isNotEmpty()) {
            viewModelScope.launch {
                try {
                    engine.strumChord(strumNotes, delayMs = 45L)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // play single note of chord
    fun playSingleNote(stringIndex: Int, engine: EarTrainerAudioEngine) {
        val voicing = getCurrentVoicing() ?: return
        val fret = voicing.frets.getOrNull(stringIndex) ?: return
        if (fret >= 0) {
            val baseMidi = stringBases[stringIndex]
            val midi = baseMidi + fret

            // Apply standard octave folding to fold note cleanly into C3 (48) to B4 (71) bounds
            var m = midi
            while (m > 71) {
                m -= 12
            }
            while (m < 48) {
                m += 12
            }

            val pitchClass = m % 12
            val octave = (m / 12) - 1
            val noteName = "${noteNames[pitchClass]}$octave"
            engine.playNote(noteName)
        }
    }
}
