package com.example.guitarkaizen.data

data class GuitarString(
    val name: String,         // e.g. "E2"
    val noteName: String,     // e.g. "E"
    val frequencyHz: Double,  // Exact frequency in Hz (A4 = 440Hz baseline)
    val index: Int            // 0 = High string (1st), 5 = Low string (6th)
)

data class Tuning(
    val id: String,
    val name: String,
    val description: String,
    val strings: List<GuitarString>
)

object TuningDictionary {
    val tunings: List<Tuning> = listOf(
        Tuning(
            id = "chromatic",
            name = "Chromatic",
            description = "All Notes",
            strings = emptyList()
        ),
        Tuning(
            id = "standard",
            name = "Standard",
            description = "E A D G B e",
            strings = listOf(
                GuitarString("E4", "E", 329.63, 0),
                GuitarString("B3", "B", 246.94, 1),
                GuitarString("G3", "G", 196.00, 2),
                GuitarString("D3", "D", 146.83, 3),
                GuitarString("A2", "A", 110.00, 4),
                GuitarString("E2", "E", 82.41, 5)
            )
        ),
        Tuning(
            id = "drop_d",
            name = "Drop D",
            description = "D A D G B e",
            strings = listOf(
                GuitarString("E4", "E", 329.63, 0),
                GuitarString("B3", "B", 246.94, 1),
                GuitarString("G3", "G", 196.00, 2),
                GuitarString("D3", "D", 146.83, 3),
                GuitarString("A2", "A", 110.00, 4),
                GuitarString("D2", "D", 73.42, 5)
            )
        ),
        Tuning(
            id = "half_step_down",
            name = "Half Step Down",
            description = "Eb Ab Db Gb Bb eb",
            strings = listOf(
                GuitarString("Eb4", "Eb", 311.13, 0),
                GuitarString("Bb3", "Bb", 233.08, 1),
                GuitarString("Gb3", "Gb", 185.00, 2),
                GuitarString("Db3", "Db", 138.59, 3),
                GuitarString("Ab2", "Ab", 103.83, 4),
                GuitarString("Eb2", "Eb", 77.78, 5)
            )
        ),
        Tuning(
            id = "open_g",
            name = "Open G",
            description = "D G D G B d",
            strings = listOf(
                GuitarString("D4", "D", 293.66, 0),
                GuitarString("B3", "B", 246.94, 1),
                GuitarString("G3", "G", 196.00, 2),
                GuitarString("D3", "D", 146.83, 3),
                GuitarString("G2", "G", 98.00, 4),
                GuitarString("D2", "D", 73.42, 5)
            )
        ),
        Tuning(
            id = "open_c",
            name = "Open C",
            description = "E C G C G C",
            strings = listOf(
                GuitarString("E4", "E", 329.63, 0),
                GuitarString("C4", "C", 261.63, 1),
                GuitarString("G3", "G", 196.00, 2),
                GuitarString("C3", "C", 130.81, 3),
                GuitarString("G2", "G", 98.00, 4),
                GuitarString("C2", "C", 65.41, 5)
            )
        ),
        Tuning(
            id = "dadgad",
            name = "DADGAD",
            description = "d a g d a d",
            strings = listOf(
                GuitarString("D4", "D", 293.66, 0),
                GuitarString("A3", "A", 220.00, 1),
                GuitarString("G3", "G", 196.00, 2),
                GuitarString("D3", "D", 146.83, 3),
                GuitarString("A2", "A", 110.00, 4),
                GuitarString("D2", "D", 73.42, 5)
            )
        )
    )
}
