package com.example.guitarkaizen.ui.screens.fretboardexplorer

import androidx.lifecycle.ViewModel
import com.example.guitarkaizen.data.ScaleDictionary
import com.example.guitarkaizen.data.ScalePattern
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Representation of a single mapped note on the virtual fretboard.
 */
data class ExplorerNote(
    val stringIndex: Int,       // 0 = Low E, 5 = High E (standard tuning string mapping)
    val fret: Int,              // Fret number from 0 (open string) to 22
    val noteName: String,       // Standard note name (e.g. "C", "F#")
    val audioNoteName: String,  // Octave-folded note name for sampler playback (e.g. "C3", "G#4")
    val midiValue: Int,         // Absolute MIDI pitch of the fret
    val absoluteMidi: Int,      // Absolute MIDI note value (fretboard pitch)
    val intervalOffset: Int,    // Semitone offset from the scale's root note (0-11)
    val degreeName: String,     // Scale degree name (e.g. "R", "b3", "5")
    val isRoot: Boolean         // True if this note is the root of the scale/arpeggio
)

/**
 * UI State for the Fretboard Explorer screen.
 */
data class FretboardExplorerState(
    val selectedKey: String = "C",
    val selectedScale: String = "Major",
    val selectedCagedPosition: String = "Any Position",
    val notes: List<ExplorerNote> = emptyList()
)

class FretboardExplorerViewModel : ViewModel() {

    private val _state = MutableStateFlow(FretboardExplorerState())
    val state: StateFlow<FretboardExplorerState> = _state.asStateFlow()

    private val stringBases = listOf(40, 45, 50, 55, 59, 64) // E2, A2, D3, G3, B3, E4 in standard tuning MIDI
    private val noteNames = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")

    private val intervalToDegree = mapOf(
        0 to "R",
        1 to "b2",
        2 to "2",
        3 to "b3",
        4 to "3",
        5 to "4",
        6 to "b5",
        7 to "5",
        8 to "b6",
        9 to "6",
        10 to "b7",
        11 to "7"
    )

    init {
        recalculateNotes()
    }

    /**
     * Updates the active root/key and triggers a recalculation of active fretboard notes.
     */
    fun setKey(key: String) {
        _state.update { it.copy(selectedKey = key) }
        recalculateNotes()
    }

    /**
     * Updates the active scale/arpeggio pattern and triggers note mapping recalculation.
     */
    fun setScale(scaleName: String) {
        _state.update { it.copy(selectedScale = scaleName) }
        recalculateNotes()
    }

    /**
     * Updates the active CAGED positional filter shape and triggers note mapping recalculation.
     */
    fun setCagedPosition(position: String) {
        _state.update { it.copy(selectedCagedPosition = position) }
        recalculateNotes()
    }

    /**
     * Recalculates and maps the notes of the active scale/arpeggio across a 22-fret matrix.
     */
    private fun recalculateNotes() {
        val current = _state.value
        val rootPC = ScaleDictionary.getPitchClass(current.selectedKey)
        val pattern = ScaleDictionary.scalePatterns.firstOrNull { it.name == current.selectedScale }
            ?: ScaleDictionary.scalePatterns.first()

        val scalePitchClasses = pattern.intervals.map { (rootPC + it) % 12 }.toSet()
        val pcToInterval = pattern.intervals.associateBy { (rootPC + it) % 12 }

        val cagedShape = current.selectedCagedPosition
        val hasCagedFilter = cagedShape != "Any Position"

        val boundingBoxes = mutableListOf<IntRange>()
        if (hasCagedFilter) {
            val refStringIdx = when (cagedShape) {
                "C Shape", "A Shape" -> 1 // A string
                "G Shape", "E Shape" -> 0 // E string
                "D Shape" -> 2           // D string
                else -> 0
            }
            val baseMidi = stringBases[refStringIdx]
            
            // Scan the entire anchor string (frets 0-22) to find all frets matching the root note
            for (fret in 0..22) {
                val midiVal = baseMidi + fret
                val fretPC = midiVal % 12
                if (fretPC == rootPC) {
                    val (start, end) = when (cagedShape) {
                        "C Shape", "G Shape" -> Pair(fret - 3, fret + 1)
                        "A Shape", "E Shape", "D Shape" -> Pair(fret - 1, fret + 3)
                        else -> Pair(0, 22)
                    }
                    boundingBoxes.add(start..end)
                }
            }
        }

        val mappedNotes = mutableListOf<ExplorerNote>()

        for (stringIdx in 0..5) {
            val baseMidi = stringBases[stringIdx]
            for (fret in 0..22) {
                val midiVal = baseMidi + fret
                val fretPC = midiVal % 12

                if (fretPC in scalePitchClasses) {
                    if (hasCagedFilter && boundingBoxes.none { fret in it }) {
                        continue
                    }

                    val intervalOffset = pcToInterval[fretPC] ?: 0
                    val degreeName = intervalToDegree[intervalOffset] ?: intervalOffset.toString()
                    val noteName = noteNames[fretPC]

                    // Calculate octave-folded note name (C3 = 48 to B4 = 71) for standard sound sampler
                    var foldedMidi = midiVal
                    while (foldedMidi > 71) {
                        foldedMidi -= 12
                    }
                    while (foldedMidi < 48) {
                        foldedMidi += 12
                    }
                    val foldedPC = foldedMidi % 12
                    val foldedOctave = (foldedMidi / 12) - 1
                    val audioNoteName = "${noteNames[foldedPC]}$foldedOctave"

                    val isRoot = (fretPC == rootPC)

                    mappedNotes.add(
                        ExplorerNote(
                            stringIndex = stringIdx,
                            fret = fret,
                            noteName = noteName,
                            audioNoteName = audioNoteName,
                            midiValue = midiVal,
                            absoluteMidi = midiVal,
                            intervalOffset = intervalOffset,
                            degreeName = degreeName,
                            isRoot = isRoot
                        )
                    )
                }
            }
        }

        _state.update { it.copy(notes = mappedNotes) }
    }
}
