package com.stringly.app.metronome.model

data class TimeSignature(
    val numerator: Int = 4,   // Beats per bar (2, 3, 4, 6, etc.)
    val denominator: Int = 4  // Note value for one beat (4 = quarter note, 8 = eighth note)
) {
    val label: String
        get() = "$numerator/$denominator"
}

enum class Subdivision(val ticksPerBeat: Int, val label: String) {
    QUARTER(1, "1/4 (Quarter)"),
    EIGHTH(2, "1/8 (Eighth)"),
    TRIPLET(3, "1/3 (Triplet)"),
    SIXTEENTH(4, "1/16 (Sixteenth)")
}

enum class MetronomeStatus {
    STOPPED,
    RUNNING,
    PAUSED
}

enum class ClickType {
    ACCENT,      // First beat of a bar
    NORMAL,      // Main beats 2..N of a bar
    SUBDIVISION  // Sub-ticks within a beat
}

data class MetronomeState(
    val status: MetronomeStatus = MetronomeStatus.STOPPED,
    val bpm: Int = 100,
    val timeSignature: TimeSignature = TimeSignature(4, 4),
    val currentBeat: Int = 1,
    val currentBar: Int = 1,
    val currentSubTick: Int = 1,
    val isAccentBeat: Boolean = true,
    val subdivision: Subdivision = Subdivision.QUARTER,
    val isMuted: Boolean = false,
    val volume: Float = 1.0f,
    val tempoMarking: String = "Andante"
) {
    companion object {
        fun getTempoMarking(bpm: Int): String {
            return when {
                bpm < 60 -> "Largo"
                bpm < 76 -> "Adagio"
                bpm < 108 -> "Andante"
                bpm < 120 -> "Moderato"
                bpm < 168 -> "Allegro"
                bpm < 200 -> "Presto"
                else -> "Prestissimo"
            }
        }
    }
}
