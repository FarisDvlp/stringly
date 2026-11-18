package com.stringly.app.tuner

import kotlin.math.sqrt

class YinPitchDetector(
    private val sampleRate: Int,
    private val bufferSize: Int = 4096,
    private val threshold: Float = 0.15f
) {
    private val yinBuffer = FloatArray(bufferSize / 2)

    data class PitchResult(
        val frequency: Float,
        val confidence: Float,
        val isPitchDetected: Boolean,
        val rms: Float
    )

    /**
     * Processes PCM 16-bit audio samples (ShortArray) and returns detected fundamental frequency.
     */
    fun detectPitch(pcmData: ShortArray, readSize: Int): PitchResult {
        if (readSize <= 0) {
            return PitchResult(-1f, 0f, false, 0f)
        }

        // 1. Calculate RMS energy to detect silence or low signal strength
        var sumSquares = 0.0
        val numSamples = minOf(readSize, bufferSize)
        val floatSamples = FloatArray(numSamples)

        for (i in 0 until numSamples) {
            val sampleNormalized = pcmData[i] / 32768.0f
            floatSamples[i] = sampleNormalized
            sumSquares += (sampleNormalized * sampleNormalized).toDouble()
        }

        val rms = sqrt((sumSquares / numSamples).toFloat())

        // Silence threshold: If signal is too weak, ignore
        if (rms < 0.008f) {
            return PitchResult(-1f, 0f, false, rms)
        }

        val halfBuffer = numSamples / 2
        if (halfBuffer <= 0) {
            return PitchResult(-1f, 0f, false, rms)
        }

        // 2. YIN Difference function
        // Min frequency ~65 Hz (E2 = 82.41 Hz), Max frequency ~1000 Hz
        val tauMin = (sampleRate / 1000.0f).toInt().coerceAtLeast(2)
        val tauMax = (sampleRate / 65.0f).toInt().coerceAtMost(halfBuffer - 1)

        yinBuffer.fill(0f)

        for (tau in tauMin..tauMax) {
            var diffSum = 0f
            for (i in 0 until halfBuffer) {
                val delta = floatSamples[i] - floatSamples[i + tau]
                diffSum += delta * delta
            }
            yinBuffer[tau] = diffSum
        }

        // 3. Cumulative mean normalized difference
        var runningSum = 0f
        yinBuffer[0] = 1f

        for (tau in 1..tauMax) {
            runningSum += yinBuffer[tau]
            yinBuffer[tau] = if (runningSum > 0f) {
                (yinBuffer[tau] * tau) / runningSum
            } else {
                1f
            }
        }

        // 4. Absolute thresholding & finding local minimum
        var tauEstimate = -1
        for (tau in tauMin..tauMax) {
            if (yinBuffer[tau] < threshold) {
                var currentTau = tau
                while (currentTau + 1 <= tauMax && yinBuffer[currentTau + 1] < yinBuffer[currentTau]) {
                    currentTau++
                }
                tauEstimate = currentTau
                break
            }
        }

        // If no value fell below absolute threshold, find global minimum in range
        if (tauEstimate == -1) {
            var minVal = Float.MAX_VALUE
            for (tau in tauMin..tauMax) {
                if (yinBuffer[tau] < minVal) {
                    minVal = yinBuffer[tau]
                    tauEstimate = tau
                }
            }
            // If even global minimum is too high, signal has low confidence / no pitch
            if (minVal > 0.40f) {
                return PitchResult(-1f, 0f, false, rms)
            }
        }

        if (tauEstimate < tauMin || tauEstimate >= tauMax) {
            return PitchResult(-1f, 0f, false, rms)
        }

        // 5. Parabolic interpolation for sub-sample accuracy
        val s0 = yinBuffer[tauEstimate - 1]
        val s1 = yinBuffer[tauEstimate]
        val s2 = yinBuffer[tauEstimate + 1]

        val denominator = 2f * (2f * s1 - s0 - s2)
        val deltaTau = if (denominator != 0f) {
            (s2 - s0) / denominator
        } else {
            0f
        }

        val refinedTau = tauEstimate + deltaTau
        val frequency = if (refinedTau > 0f) sampleRate / refinedTau else -1f
        val confidence = (1f - yinBuffer[tauEstimate]).coerceIn(0f, 1f)

        // Validate target frequency range for guitar (approx 65 Hz to 450 Hz)
        if (frequency !in 65.0f..450.0f) {
            return PitchResult(-1f, confidence, false, rms)
        }

        return PitchResult(
            frequency = frequency,
            confidence = confidence,
            isPitchDetected = true,
            rms = rms
        )
    }
}
