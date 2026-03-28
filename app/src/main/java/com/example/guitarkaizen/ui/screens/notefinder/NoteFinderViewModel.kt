package com.example.guitarkaizen.ui.screens.notefinder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

data class NoteFinderState(
    val currentString: Int = 5,    // 0 = String 1 (High E), 5 = String 6 (Low E)
    val currentFret: Int = 3,      // Default to Fret 3 on Low E (G)
    val currentNoteName: String = "G",
    val sessionScore: Int = 0,
    val totalGuesses: Int = 0,
    val selectedMinFret: Int = 0,
    val selectedMaxFret: Int = 12,
    val activeStrings: List<Boolean> = List(6) { true },
    val selectedKeyLock: String = "ALL", // ALL, C, G, D, A, E, F
    val lastGuessResult: Boolean? = null, // null = none, true = correct, false = wrong
    val lastGuessedNote: String = ""
)

class NoteFinderViewModel : ViewModel() {

    private val _state = MutableStateFlow(NoteFinderState())
    val state: StateFlow<NoteFinderState> = _state.asStateFlow()

    private val openPitches = intArrayOf(4, 11, 7, 2, 9, 4) // [E4, B3, G3, D3, A2, E2]
    private val noteNames = listOf("C", "C#/Db", "D", "D#/Eb", "E", "F", "F#/Gb", "G", "G#/Ab", "A", "A#/Bb", "B")

    init {
        generateNextQuestion()
    }

    fun guessNote(guess: String) {
        val currentState = _state.value
        val isCorrect = guess == currentState.currentNoteName

        if (isCorrect) {
            _state.update {
                it.copy(
                    sessionScore = currentState.sessionScore + 1,
                    totalGuesses = currentState.totalGuesses + 1,
                    lastGuessResult = true,
                    lastGuessedNote = guess
                )
            }
            // Auto advance after 1000ms delay
            viewModelScope.launch {
                delay(1000)
                generateNextQuestion()
            }
        } else {
            _state.update {
                it.copy(
                    totalGuesses = currentState.totalGuesses + 1,
                    lastGuessResult = false,
                    lastGuessedNote = guess
                )
            }
        }
    }

    fun generateNextQuestion() {
        val currentState = _state.value
        val validOptions = mutableListOf<Pair<Int, Int>>()

        // Diatonic key lock sets
        val keySet = when (currentState.selectedKeyLock) {
            "C" -> setOf(0, 2, 4, 5, 7, 9, 11)
            "G" -> setOf(7, 9, 11, 0, 2, 4, 6)
            "D" -> setOf(2, 4, 6, 7, 9, 11, 1)
            "A" -> setOf(9, 11, 1, 2, 4, 6, 8)
            "E" -> setOf(4, 6, 8, 9, 11, 1, 3)
            "F" -> setOf(5, 7, 9, 10, 0, 2, 4)
            else -> setOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11) // "ALL"
        }

        for (s in 0..5) {
            if (currentState.activeStrings[s]) {
                for (f in currentState.selectedMinFret..currentState.selectedMaxFret) {
                    val pc = (openPitches[s] + f) % 12
                    if (pc in keySet) {
                        // Avoid repeating the same position immediately if options are abundant
                        if (s != currentState.currentString || f != currentState.currentFret || validOptions.isEmpty()) {
                            validOptions.add(Pair(s, f))
                        }
                    }
                }
            }
        }

        if (validOptions.isNotEmpty()) {
            val (newString, newFret) = validOptions.random()
            val pc = (openPitches[newString] + newFret) % 12
            _state.update {
                it.copy(
                    currentString = newString,
                    currentFret = newFret,
                    currentNoteName = noteNames[pc],
                    lastGuessResult = null,
                    lastGuessedNote = ""
                )
            }
        }
    }

    fun setMinFret(min: Int) {
        val clampedMin = min.coerceIn(0, 24)
        val max = maxOf(clampedMin, _state.value.selectedMaxFret)
        _state.update {
            it.copy(
                selectedMinFret = clampedMin,
                selectedMaxFret = max
            )
        }
        verifyAndRegenerateIfInvalid()
    }

    fun setMaxFret(max: Int) {
        val clampedMax = max.coerceIn(0, 24)
        val min = minOf(clampedMax, _state.value.selectedMinFret)
        _state.update {
            it.copy(
                selectedMinFret = min,
                selectedMaxFret = clampedMax
            )
        }
        verifyAndRegenerateIfInvalid()
    }

    fun toggleString(stringIndex: Int) {
        if (stringIndex in 0..5) {
            val newActive = _state.value.activeStrings.toMutableList()
            newActive[stringIndex] = !newActive[stringIndex]
            
            // Ensure at least one string remains active
            if (newActive.any { it }) {
                _state.update { it.copy(activeStrings = newActive) }
                verifyAndRegenerateIfInvalid()
            }
        }
    }

    fun setKeyLock(key: String) {
        _state.update { it.copy(selectedKeyLock = key) }
        verifyAndRegenerateIfInvalid()
    }

    fun resetStats() {
        _state.update {
            it.copy(
                sessionScore = 0,
                totalGuesses = 0
            )
        }
        generateNextQuestion()
    }

    private fun verifyAndRegenerateIfInvalid() {
        val currentState = _state.value
        val pc = (openPitches[currentState.currentString] + currentState.currentFret) % 12

        // Diatonic key check
        val isDiatonic = when (currentState.selectedKeyLock) {
            "C" -> pc in setOf(0, 2, 4, 5, 7, 9, 11)
            "G" -> pc in setOf(7, 9, 11, 0, 2, 4, 6)
            "D" -> pc in setOf(2, 4, 6, 7, 9, 11, 1)
            "A" -> pc in setOf(9, 11, 1, 2, 4, 6, 8)
            "E" -> pc in setOf(4, 6, 8, 9, 11, 1, 3)
            "F" -> pc in setOf(5, 7, 9, 10, 0, 2, 4)
            else -> true
        }

        // If the current note is no longer in the active ranges, regenerate
        val isFretValid = currentState.currentFret in currentState.selectedMinFret..currentState.selectedMaxFret
        val isStringValid = currentState.activeStrings[currentState.currentString]

        if (!isFretValid || !isStringValid || !isDiatonic) {
            generateNextQuestion()
        }
    }
}
