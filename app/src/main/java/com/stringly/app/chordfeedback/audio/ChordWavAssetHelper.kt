package com.stringly.app.chordfeedback.audio

import com.stringly.app.chordfeedback.model.ChordData
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

object ChordWavAssetHelper {

    private const val SAMPLE_RATE = 44100
    private const val DURATION_SECONDS = 1.8

    /**
     * Generates a 16-bit PCM Mono WAV byte array representing a realistic strummed guitar chord.
     */
    fun generateChordWavData(chordData: ChordData): ByteArray {
        val numSamples = (SAMPLE_RATE * DURATION_SECONDS).toInt()
        val pcm = ShortArray(numSamples)
        val freqs = chordData.stringFrequencies

        val numStrings = freqs.size
        val strumDelayPerStringSamples = (SAMPLE_RATE * 0.035).toInt() // 35ms strum gap between strings

        for (stringIdx in 0 until numStrings) {
            val freq = freqs[stringIdx].toDouble()
            val startSample = stringIdx * strumDelayPerStringSamples

            for (i in startSample until numSamples) {
                val t = (i - startSample).toDouble() / SAMPLE_RATE
                // Realistic exponential string decay + harmonic overtones (f0, 2f0, 3f0)
                val decay = exp(-2.2 * t)
                val fundamental = sin(2.0 * PI * freq * t)
                val harmonic2 = 0.45 * sin(2.0 * PI * (freq * 2.0) * t)
                val harmonic3 = 0.25 * sin(2.0 * PI * (freq * 3.0) * t)

                val stringSample = (fundamental + harmonic2 + harmonic3) * decay
                val combined = pcm[i] + (stringSample * 5000.0).toInt()
                pcm[i] = combined.coerceIn(-32768, 32767).toShort()
            }
        }

        return pcmToWav(pcm, SAMPLE_RATE)
    }

    /**
     * Converts a 16-bit PCM ShortArray to a standard WAV ByteArray with a 44-byte RIFF header.
     */
    fun pcmToWav(pcmData: ShortArray, sampleRate: Int): ByteArray {
        val dataSize = pcmData.size * 2
        val totalSize = 36 + dataSize
        val out = ByteArrayOutputStream(44 + dataSize)

        // RIFF header
        out.write(byteArrayOf('R'.code.toByte(), 'I'.code.toByte(), 'F'.code.toByte(), 'F'.code.toByte()))
        out.write(intToByteArrayLittleEndian(totalSize))
        out.write(byteArrayOf('W'.code.toByte(), 'A'.code.toByte(), 'V'.code.toByte(), 'E'.code.toByte()))

        // fmt chunk
        out.write(byteArrayOf('f'.code.toByte(), 'm'.code.toByte(), 't'.code.toByte(), ' '.code.toByte()))
        out.write(intToByteArrayLittleEndian(16)) // Subchunk1Size (16 for PCM)
        out.write(shortToByteArrayLittleEndian(1.toShort())) // AudioFormat (1 = PCM)
        out.write(shortToByteArrayLittleEndian(1.toShort())) // NumChannels (1 = Mono)
        out.write(intToByteArrayLittleEndian(sampleRate))
        out.write(intToByteArrayLittleEndian(sampleRate * 2)) // ByteRate
        out.write(shortToByteArrayLittleEndian(2.toShort())) // BlockAlign
        out.write(shortToByteArrayLittleEndian(16.toShort())) // BitsPerSample

        // data chunk
        out.write(byteArrayOf('d'.code.toByte(), 'a'.code.toByte(), 't'.code.toByte(), 'a'.code.toByte()))
        out.write(intToByteArrayLittleEndian(dataSize))

        // PCM Sample Data
        for (sample in pcmData) {
            out.write(shortToByteArrayLittleEndian(sample))
        }

        return out.toByteArray()
    }

    /**
     * Ensures WAV files exist in the specified output directory.
     */
    fun ensureWavFilesExist(directory: File, chords: List<ChordData>) {
        if (!directory.exists()) {
            directory.mkdirs()
        }
        for (chord in chords) {
            val fileName = chord.audioAssetName.substringAfterLast("/")
            val file = File(directory, fileName)
            if (!file.exists() || file.length() < 100) {
                val wavData = generateChordWavData(chord)
                FileOutputStream(file).use { it.write(wavData) }
            }
        }
    }

    private fun intToByteArrayLittleEndian(value: Int): ByteArray {
        return byteArrayOf(
            (value and 0xFF).toByte(),
            ((value shr 8) and 0xFF).toByte(),
            ((value shr 16) and 0xFF).toByte(),
            ((value shr 24) and 0xFF).toByte()
        )
    }

    private fun shortToByteArrayLittleEndian(value: Short): ByteArray {
        val i = value.toInt()
        return byteArrayOf(
            (i and 0xFF).toByte(),
            ((i shr 8) and 0xFF).toByte()
        )
    }
}
