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

    fun tap(currentTimeMs: Long = System.currentTimeMillis()): Int? {
        if (lastTapTimeMs == 0L || (currentTimeMs - lastTapTimeMs) > timeoutMs) {
            reset()
            lastTapTimeMs = currentTimeMs
            return null
        }

        val interval = currentTimeMs - lastTapTimeMs
        lastTapTimeMs = currentTimeMs

        val minIntervalMs = (60_000.0 / maxBpm).toLong()
        val maxIntervalMs = (60_000.0 / minBpm).toLong()

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
