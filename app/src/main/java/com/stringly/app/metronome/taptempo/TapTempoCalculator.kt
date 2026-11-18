package com.stringly.app.metronome.taptempo

import kotlin.math.roundToInt

class TapTempoCalculator(
    private val maxHistorySize: Int = 5,
    private val timeoutMs: Long = 2500L,
    private val minBpm: Int = 30,
    private val maxBpm: Int = 300
) {
    private var lastTapTimeMs: Long = 0L
    private val tapIntervals = ArrayDeque<Long>()

    /**
     * Records a tap at [currentTimeMs].
     * Returns the newly calculated BPM if enough taps were recorded, or null otherwise.
     */
    fun tap(currentTimeMs: Long = System.currentTimeMillis()): Int? {
        if (lastTapTimeMs == 0L || (currentTimeMs - lastTapTimeMs) > timeoutMs) {
            // Reset tap history if first tap or gap exceeded timeout
            reset()
            lastTapTimeMs = currentTimeMs
            return null
        }

        val interval = currentTimeMs - lastTapTimeMs
        lastTapTimeMs = currentTimeMs

        // Validate interval bounds (min 30 BPM -> max 2000ms, max 300 BPM -> min 200ms)
        val minIntervalMs = (60_000.0 / maxBpm).toLong() // 200ms
        val maxIntervalMs = (60_000.0 / minBpm).toLong() // 2000ms

        if (interval !in minIntervalMs..maxIntervalMs) {
            reset()
            lastTapTimeMs = currentTimeMs
            return null
        }

        tapIntervals.addLast(interval)
        if (tapIntervals.size > maxHistorySize) {
            tapIntervals.removeFirst()
        }

        if (tapIntervals.isEmpty()) return null

        // Use median interval for stability
        val sortedIntervals = tapIntervals.sorted()
        val medianInterval = sortedIntervals[sortedIntervals.size / 2]

        val calculatedBpm = (60_000.0 / medianInterval).roundToInt()
        return calculatedBpm.coerceIn(minBpm, maxBpm)
    }

    fun reset() {
        lastTapTimeMs = 0L
        tapIntervals.clear()
    }
}
