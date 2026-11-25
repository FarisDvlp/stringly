package com.stringly.app.chordfeedback.model

enum class EvaluationResult {
    CORRECT,
    INCORRECT,
    UNSUPPORTED,
    UNCLEAR
}

data class ChordData(
    val name: String,              // e.g., "A", "B", "C", "D", "E", "F", "G"
    val fullName: String,          // e.g., "A Major"
    val rootPitchClass: Int,       // 0=C, 1=C#, 2=D, 3=D#, 4=E, 5=F, 6=F#, 7=G, 8=G#, 9=A, 10=A#, 11=B
    val thirdPitchClass: Int,      // Major 3rd pitch class
    val fifthPitchClass: Int,      // Perfect 5th pitch class
    val pitchClasses: List<Int>,   // List of 3 triad pitch classes
    val formula: String,           // "1 - 3 - 5"
    val fingering: String,         // e.g., "x-0-2-2-2-0"
    val frets: List<Int>,          // e.g., [-1, 0, 2, 2, 2, 0]
    val stringFrequencies: List<Float>, // Frequencies for played strings
    val audioAssetName: String     // e.g., "chords/chord_a.wav"
)

data class ChordFeedbackState(
    val selectedChord: ChordData = ChordConstants.MAJOR_CHORDS[0],
    val isRecording: Boolean = false,
    val isPlayingReference: Boolean = false,
    val result: EvaluationResult = EvaluationResult.UNCLEAR,
    val detectedChordName: String? = null,
    val triadCompletenessScore: Float = 0f,
    val chordSimilarityScore: Float = 0f,
    val signalQualityScore: Float = 0f,
    val feedbackMessage: String = "Select a chord and tap Start Practice"
)

object ChordConstants {

    // Pitch Class Index Mapping
    // 0=C, 1=C#, 2=D, 3=D#, 4=E, 5=F, 6=F#, 7=G, 8=G#, 9=A, 10=A#, 11=B
    val A_MAJOR = ChordData(
        name = "A",
        fullName = "A Major",
        rootPitchClass = 9,   // A
        thirdPitchClass = 1,  // C#
        fifthPitchClass = 4,  // E
        pitchClasses = listOf(9, 1, 4),
        formula = "1 - 3 - 5 (A - C# - E)",
        fingering = "x-0-2-2-2-0",
        frets = listOf(-1, 0, 2, 2, 2, 0),
        stringFrequencies = listOf(110.00f, 146.83f, 220.00f, 277.18f, 329.63f),
        audioAssetName = "chords/chord_a.wav"
    )

    val B_MAJOR = ChordData(
        name = "B",
        fullName = "B Major",
        rootPitchClass = 11,  // B
        thirdPitchClass = 3,  // D#
        fifthPitchClass = 6,  // F#
        pitchClasses = listOf(11, 3, 6),
        formula = "1 - 3 - 5 (B - D# - F#)",
        fingering = "x-2-4-4-4-2",
        frets = listOf(-1, 2, 4, 4, 4, 2),
        stringFrequencies = listOf(123.47f, 185.00f, 246.94f, 311.13f, 369.99f),
        audioAssetName = "chords/chord_b.wav"
    )

    val C_MAJOR = ChordData(
        name = "C",
        fullName = "C Major",
        rootPitchClass = 0,   // C
        thirdPitchClass = 4,  // E
        fifthPitchClass = 7,  // G
        pitchClasses = listOf(0, 4, 7),
        formula = "1 - 3 - 5 (C - E - G)",
        fingering = "x-3-2-0-1-0",
        frets = listOf(-1, 3, 2, 0, 1, 0),
        stringFrequencies = listOf(130.81f, 164.81f, 196.00f, 261.63f, 329.63f),
        audioAssetName = "chords/chord_c.wav"
    )

    val D_MAJOR = ChordData(
        name = "D",
        fullName = "D Major",
        rootPitchClass = 2,   // D
        thirdPitchClass = 6,  // F#
        fifthPitchClass = 9,  // A
        pitchClasses = listOf(2, 6, 9),
        formula = "1 - 3 - 5 (D - F# - A)",
        fingering = "x-x-0-2-3-2",
        frets = listOf(-1, -1, 0, 2, 3, 2),
        stringFrequencies = listOf(146.83f, 220.00f, 293.66f, 369.99f),
        audioAssetName = "chords/chord_d.wav"
    )

    val E_MAJOR = ChordData(
        name = "E",
        fullName = "E Major",
        rootPitchClass = 4,   // E
        thirdPitchClass = 8,  // G#
        fifthPitchClass = 11, // B
        pitchClasses = listOf(4, 8, 11),
        formula = "1 - 3 - 5 (E - G# - B)",
        fingering = "0-2-2-1-0-0",
        frets = listOf(0, 2, 2, 1, 0, 0),
        stringFrequencies = listOf(82.41f, 123.47f, 164.81f, 207.65f, 246.94f, 329.63f),
        audioAssetName = "chords/chord_e.wav"
    )

    val F_MAJOR = ChordData(
        name = "F",
        fullName = "F Major",
        rootPitchClass = 5,   // F
        thirdPitchClass = 9,  // A
        fifthPitchClass = 0,  // C
        pitchClasses = listOf(5, 9, 0),
        formula = "1 - 3 - 5 (F - A - C)",
        fingering = "1-3-3-2-1-1",
        frets = listOf(1, 3, 3, 2, 1, 1),
        stringFrequencies = listOf(87.31f, 130.81f, 174.61f, 220.00f, 261.63f, 349.23f),
        audioAssetName = "chords/chord_f.wav"
    )

    val G_MAJOR = ChordData(
        name = "G",
        fullName = "G Major",
        rootPitchClass = 7,   // G
        thirdPitchClass = 11, // B
        fifthPitchClass = 2,  // D
        pitchClasses = listOf(7, 11, 2),
        formula = "1 - 3 - 5 (G - B - D)",
        fingering = "3-2-0-0-0-3",
        frets = listOf(3, 2, 0, 0, 0, 3),
        stringFrequencies = listOf(98.00f, 123.47f, 146.83f, 196.00f, 246.94f, 392.00f),
        audioAssetName = "chords/chord_g.wav"
    )

    val MAJOR_CHORDS = listOf(
        A_MAJOR,
        B_MAJOR,
        C_MAJOR,
        D_MAJOR,
        E_MAJOR,
        F_MAJOR,
        G_MAJOR
    )

    fun getChordByName(name: String): ChordData? {
        val clean = name.trim().uppercase()
        return MAJOR_CHORDS.firstOrNull { it.name.uppercase() == clean || it.fullName.uppercase() == clean }
    }
}
