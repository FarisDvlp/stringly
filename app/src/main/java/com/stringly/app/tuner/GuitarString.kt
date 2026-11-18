package com.stringly.app.tuner

import kotlin.math.abs
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.roundToInt

data class GuitarString(
    val name: String,
    val octave: Int,
    val frequency: Float,
    val stringNumber: Int, // 1 to 6
)

enum class TuningStatus {
    LISTENING,
    IN_TUNE,
    FLAT,
    SHARP,
    NO_PERMISSION,
    ERROR
}

object GuitarTunerConstants {
    val STANDARD_STRINGS = listOf(
        GuitarString("E", 2, 82.41f, 6),
        GuitarString("A", 2, 110.00f, 5),
        GuitarString("D", 3, 146.83f, 4),
        GuitarString("G", 3, 196.00f, 3),
        GuitarString("B", 3, 246.94f, 2),
        GuitarString("E", 4, 329.63f, 1)
    )

    const val IN_TUNE_TOLERANCE_CENTS = 5.0f

    /**
     * Calculates pitch deviation in cents between actual frequency and target frequency.
     * Cents = 1200 * log2(actual / target)
     */
    fun calculateCents(actualFreq: Float, targetFreq: Float): Float {
        if ((actualFreq <= 0f) || (targetFreq <= 0f)) return 0f
        return (1200.0 * log2(actualFreq.toDouble() / targetFreq.toDouble())).toFloat()
    }

    /**
     * Finds nearest guitar string from standard tuning given a detected frequency.
     */
    fun findNearestString(frequency: Float): GuitarString {
        if (frequency <= 0f) return STANDARD_STRINGS[0]
        return STANDARD_STRINGS.minByOrNull { string ->
            abs(calculateCents(frequency, string.frequency))
        } ?: STANDARD_STRINGS[0]
    }

    /**
     * Converts frequency to note name and octave (Chromatic identification).
     */
    fun frequencyToNoteName(frequency: Float): Pair<String, Int> {
        if (frequency <= 0f) return Pair("-", 0)
        // A4 = 440 Hz is note 69
        val noteNum = (69 + 12 * log2(frequency.toDouble() / 440.0)).roundToInt()
        val noteNames = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
        val noteIndex = ((noteNum % 12) + 12) % 12
        val octave = (noteNum / 12) - 1
        return Pair(noteNames[noteIndex], octave)
    }

    /**
     * Gets target frequency for a chromatic note number.
     */
    fun noteNumberToFrequency(noteNum: Int): Float {
        return (440.0 * 2.0.pow((noteNum - 69) / 12.0)).toFloat()
    }
}
