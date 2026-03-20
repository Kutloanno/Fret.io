package com.example.guitarkaizen.data

// Chord shape representation
data class ChordVoicing(
    val root: String,
    val quality: String,
    val frets: List<Int>
)

// Predefined open chords & CAGED logic
object ChordDictionary {

    private val rootPitches = mapOf(
        "C" to 0, "C#" to 1, "Db" to 1, "D" to 2, "D#" to 3, "Eb" to 3,
        "E" to 4, "F" to 5, "F#" to 6, "Gb" to 6, "G" to 7, "G#" to 8,
        "Ab" to 8, "A" to 9, "A#" to 10, "Bb" to 10, "B" to 11
    )

    // open overrides
    private val openPositionOverrides = listOf(
        // Major
        ChordVoicing("C", "Major", listOf(-1, 3, 2, 0, 1, 0)),
        ChordVoicing("A", "Major", listOf(-1, 0, 2, 2, 2, 0)),
        ChordVoicing("G", "Major", listOf(3, 2, 0, 0, 0, 3)),
        ChordVoicing("E", "Major", listOf(0, 2, 2, 1, 0, 0)),
        ChordVoicing("D", "Major", listOf(-1, -1, 0, 2, 3, 2)),
        // Minor
        ChordVoicing("A", "Minor", listOf(-1, 0, 2, 2, 1, 0)),
        ChordVoicing("E", "Minor", listOf(0, 2, 2, 0, 0, 0)),
        ChordVoicing("D", "Minor", listOf(-1, -1, 0, 2, 3, 1)),
        // Dominant 7
        ChordVoicing("C", "7", listOf(-1, 3, 2, 3, 1, 0)),
        ChordVoicing("A", "7", listOf(-1, 0, 2, 0, 2, 0)),
        ChordVoicing("G", "7", listOf(3, 2, 0, 0, 0, 1)),
        ChordVoicing("E", "7", listOf(0, 2, 0, 1, 0, 0)),
        ChordVoicing("D", "7", listOf(-1, -1, 0, 2, 1, 2)),
        ChordVoicing("B", "7", listOf(-1, 2, 1, 2, 0, 2)),
        // sus4
        ChordVoicing("A", "sus4", listOf(-1, 0, 2, 2, 3, 0)),
        ChordVoicing("D", "sus4", listOf(-1, -1, 0, 2, 3, 3)),
        ChordVoicing("E", "sus4", listOf(0, 2, 2, 2, 0, 0)),
        // sus2
        ChordVoicing("A", "sus2", listOf(-1, 0, 2, 2, 0, 0)),
        ChordVoicing("D", "sus2", listOf(-1, -1, 0, 2, 3, 0))
    )

    // CAGED shapes
    // Values map to: String 6 down to 1. -1 means muted.
    // E-shape is relative to Root on String 6 ($F_6$)
    // A-shape is relative to Root on String 5 ($F_5$)
    // C-shape is relative to Root on String 5 ($F_5$, played backward)
    // D-shape is relative to Root on String 4 ($F_4$)
    // G-shape is relative to Root on String 6 ($F_6$, played backward)
    private val cagesPatterns = mapOf(
        "Major" to listOf(
            listOf(0, 2, 2, 1, 0, 0),        // E-shape
            listOf(-1, 0, 2, 2, 2, 0),       // A-shape
            listOf(-1, 0, -1, -3, -2, -3),    // C-shape
            listOf(-1, -1, 0, 2, 3, 2),      // D-shape
            listOf(0, -1, -3, -3, -3, 0)      // G-shape
        ),
        "Minor" to listOf(
            listOf(0, 2, 2, 0, 0, 0),
            listOf(-1, 0, 2, 2, 1, 0),
            listOf(-1, 0, -2, -3, -2, -4),
            listOf(-1, -1, 0, 2, 3, 1),
            listOf(0, -2, -3, -3, -3, 0)
        ),
        "Diminished" to listOf(
            listOf(0, -1, 2, 3, 2, -1),
            listOf(-1, 0, 1, 2, 1, -1),
            listOf(-1, 0, -2, -4, -2, -4),
            listOf(-1, -1, 0, 1, 3, 1),
            listOf(0, -2, -4, -3, -3, 0)
        ),
        "Augmented" to listOf(
            listOf(0, 3, 2, 1, 1, 0),
            listOf(-1, 0, 3, 2, 2, 1),
            listOf(-1, 0, -1, -2, -2, -3),
            listOf(-1, -1, 0, 2, 3, 3),
            listOf(0, 0, -3, -3, -3, 0)
        ),
        "Maj7" to listOf(
            listOf(0, -1, 1, 1, 0, -1),
            listOf(-1, 0, 2, 1, 2, 0),
            listOf(-1, 0, -1, -3, -3, -3),
            listOf(-1, -1, 0, 2, 2, 2),
            listOf(0, -1, -3, -3, -4, 0)
        ),
        "Min7" to listOf(
            listOf(0, -1, 0, 0, 0, -1),
            listOf(-1, 0, 2, 0, 1, 0),
            listOf(-1, 0, -2, -3, -3, -4),
            listOf(-1, -1, 0, 2, 1, 1),
            listOf(0, -2, -3, -3, -4, 0)
        ),
        "7" to listOf(
            listOf(0, 2, 0, 1, 0, 0),
            listOf(-1, 0, 2, 0, 2, 0),
            listOf(-1, 0, -1, -3, -3, -3),
            listOf(-1, -1, 0, 2, 1, 2),
            listOf(0, -1, -3, -3, -4, 0)
        ),
        "m7b5" to listOf(
            listOf(0, -1, 2, 3, 3, -1),
            listOf(-1, 0, 1, 0, 1, -1),
            listOf(-1, 0, -2, -4, -3, -4),
            listOf(-1, -1, 0, 1, 1, 1),
            listOf(0, -2, -4, -3, -4, 0)
        ),
        "Dim7" to listOf(
            listOf(0, -1, 2, 3, 2, -1),
            listOf(-1, 0, 1, 2, 1, -1),
            listOf(-1, 0, -2, -4, -2, -4),
            listOf(-1, -1, 0, 1, 0, 1),
            listOf(0, -2, -4, -3, -3, 0)
        ),
        "sus2" to listOf(
            listOf(0, 2, 2, -1, 0, 0),
            listOf(-1, 0, 2, 2, 0, 0),
            listOf(-1, 0, -3, -3, -3, -3),
            listOf(-1, -1, 0, 2, 3, 0),
            listOf(0, -3, -3, -3, -3, 0)
        ),
        "sus4" to listOf(
            listOf(0, 2, 2, 2, 0, 0),
            listOf(-1, 0, 2, 2, 3, 0),
            listOf(-1, 0, 0, -3, -2, -3),
            listOf(-1, -1, 0, 2, 3, 3),
            listOf(0, 0, -3, -3, -3, 0)
        ),
        "add9" to listOf(
            listOf(0, 2, 4, 1, 0, 0),
            listOf(-1, 0, 2, 4, 2, 0),
            listOf(-1, 0, -1, -3, 0, -3),
            listOf(-1, -1, 0, 2, 5, 2),
            listOf(0, -1, -3, -3, 0, 0)
        ),
        "min9" to listOf(
            listOf(0, -1, 0, 0, 0, 2),
            listOf(-1, 0, 2, 0, 1, 2),
            listOf(-1, 0, -2, -3, 0, -4),
            listOf(-1, -1, 0, 2, 1, 0),
            listOf(0, -2, -3, -3, 0, 0)
        ),
        "maj9" to listOf(
            listOf(0, -1, 1, 1, 0, 2),
            listOf(-1, 0, 2, 1, 2, 2),
            listOf(-1, 0, -1, -3, 0, -3),
            listOf(-1, -1, 0, 2, 2, 0),
            listOf(0, -1, -3, -3, 0, 0)
        )
    )

    /**
     * Synthesizes and filters an exhaustive catalog of chord voicings for the selected root and quality.
     * Computes the 5 distinct CAGED shapes dynamically, merges open-position catalog overrides,
     * and filters out any configurations extending beyond the 15th fret.
     */
    fun getVoicings(root: String, quality: String): List<ChordVoicing> {
        val rootNorm = root.trim()
        val qualityNorm = quality.trim()

        val p = rootPitches[rootNorm] ?: return emptyList()
        val patterns = cagesPatterns[qualityNorm] ?: return emptyList()

        // open overrides first
        val results = mutableListOf<ChordVoicing>()
        openPositionOverrides.forEach { override ->
            if (override.root.equals(rootNorm, ignoreCase = true) &&
                override.quality.equals(qualityNorm, ignoreCase = true)
            ) {
                results.add(override)
            }
        }

        // dynamic CAGED shape generation
        // E-shape (index 0) and G-shape (index 4) are relative to String 6 ($F_6$)
        // A-shape (index 1) and C-shape (index 2) are relative to String 5 ($F_5$)
        // D-shape (index 3) is relative to String 4 ($F_4$)
        val f6 = (p - 4 + 12) % 12
        val f5 = (p - 9 + 12) % 12
        val f4 = (p - 2 + 12) % 12

        patterns.forEachIndexed { shapeIdx, pattern ->
            val rootFret = when (shapeIdx) {
                0, 4 -> f6 // E, G shapes
                1, 2 -> f5 // A, C shapes
                3 -> f4    // D shape
                else -> 0
            }

            // offset interval
            val rawFrets = pattern.map { relVal ->
                if (relVal == -1) -1 else rootFret + relVal
            }

            // shift up if negative
            val hasNegative = rawFrets.any { it in -100..-2 } // Ignore -1
            val adjustedFrets = if (hasNegative) {
                rawFrets.map { if (it == -1) -1 else it + 12 }
            } else {
                rawFrets
            }

            // clamp to 15th fret
            val isPlayable = adjustedFrets.filter { it >= 0 }.all { it <= 15 }

            if (isPlayable) {
                results.add(ChordVoicing(rootNorm, qualityNorm, adjustedFrets))
            }
        }

        // clean duplicates
        return results.distinctBy { it.frets }
    }

    /**
     * Backward-compatibility wrapper returning all voicings in the dictionary.
     */
    val voicings: List<ChordVoicing>
        get() {
            val list = mutableListOf<ChordVoicing>()
            rootPitches.keys.distinct().forEach { r ->
                cagesPatterns.keys.forEach { q ->
                    list.addAll(getVoicings(r, q))
                }
            }
            return list
        }
}
