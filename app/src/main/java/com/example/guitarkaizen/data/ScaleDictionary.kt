package com.example.guitarkaizen.data

enum class ScaleType {
    SCALE, ARPEGGIO
}

data class ScalePattern(
    val name: String,
    val intervals: List<Int>, // Semitones relative to the root note
    val type: ScaleType
)

object ScaleDictionary {
    val keysList = listOf(
        "C", "C#", "Db", "D", "D#", "Eb", "E", "F", "F#", "Gb", "G", "G#", "Ab", "A", "A#", "Bb", "B"
    )

    val scalePatterns = listOf(
        // Scales
        ScalePattern("Major", listOf(0, 2, 4, 5, 7, 9, 11), ScaleType.SCALE),
        ScalePattern("Natural Minor", listOf(0, 2, 3, 5, 7, 8, 10), ScaleType.SCALE),
        ScalePattern("Major Pentatonic", listOf(0, 2, 4, 7, 9), ScaleType.SCALE),
        ScalePattern("Minor Pentatonic", listOf(0, 3, 5, 7, 10), ScaleType.SCALE),
        ScalePattern("Blues", listOf(0, 3, 5, 6, 7, 10), ScaleType.SCALE),
        
        // Triad Arpeggios
        ScalePattern("Major Triad", listOf(0, 4, 7), ScaleType.ARPEGGIO),
        ScalePattern("Minor Triad", listOf(0, 3, 7), ScaleType.ARPEGGIO),
        ScalePattern("Diminished Triad", listOf(0, 3, 6), ScaleType.ARPEGGIO),
        ScalePattern("Augmented Triad", listOf(0, 4, 8), ScaleType.ARPEGGIO),
        
        // Expanded Triads & 7th Chords
        ScalePattern("SUS2 TRIAD", listOf(0, 2, 7), ScaleType.ARPEGGIO),
        ScalePattern("SUS4 TRIAD", listOf(0, 5, 7), ScaleType.ARPEGGIO),
        ScalePattern("MAJ7 ARPEGGIO", listOf(0, 4, 7, 11), ScaleType.ARPEGGIO),
        ScalePattern("MIN7 ARPEGGIO", listOf(0, 3, 7, 10), ScaleType.ARPEGGIO),
        ScalePattern("DOM7 ARPEGGIO", listOf(0, 4, 7, 10), ScaleType.ARPEGGIO),
        ScalePattern("M7B5 ARPEGGIO", listOf(0, 3, 6, 10), ScaleType.ARPEGGIO),
        ScalePattern("DIM7 ARPEGGIO", listOf(0, 3, 6, 9), ScaleType.ARPEGGIO)
    )

    private val rootPitches = mapOf(
        "C" to 0, "C#" to 1, "Db" to 1, "D" to 2, "D#" to 3, "Eb" to 3,
        "E" to 4, "F" to 5, "F#" to 6, "Gb" to 6, "G" to 7, "G#" to 8,
        "Ab" to 8, "A" to 9, "A#" to 10, "Bb" to 10, "B" to 11
    )

    fun getPitchClass(note: String): Int {
        return rootPitches[note] ?: 0
    }
}
