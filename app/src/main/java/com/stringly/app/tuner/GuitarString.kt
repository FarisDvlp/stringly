package com.stringly.app.tuner

import kotlin.math.abs
import kotlin.math.log2

data class GuitarStringData(
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
        GuitarStringData("E", 2, 82.41f, 6),
        GuitarStringData("A", 2, 110.00f, 5),
        GuitarStringData("D", 3, 146.83f, 4),
        GuitarStringData("G", 3, 196.00f, 3),
        GuitarStringData("B", 3, 246.94f, 2),
        GuitarStringData("E", 4, 329.63f, 1)
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
    fun findNearestString(frequency: Float): GuitarStringData {
        if (frequency <= 0f) return STANDARD_STRINGS[0]
        return STANDARD_STRINGS.minByOrNull { string ->
            abs(calculateCents(frequency, string.frequency))
        } ?: STANDARD_STRINGS[0]
    }
}
