package com.stringly.app.metronome.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.stringly.app.metronome.model.ClickType
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

class MetronomeAudioPlayer {

    private val sampleRate = 44100

    private val accentPcm: ShortArray = generateClickPcm(frequency = 1200.0, durationMs = 12.0, maxVolume = 0.95f)
    private val normalPcm: ShortArray = generateClickPcm(frequency = 850.0, durationMs = 10.0, maxVolume = 0.80f)
    private val subPcm: ShortArray = generateClickPcm(frequency = 650.0, durationMs = 7.0, maxVolume = 0.50f)

    private var accentTrack: AudioTrack? = createAudioTrack(accentPcm)
    private var normalTrack: AudioTrack? = createAudioTrack(normalPcm)
    private var subTrack: AudioTrack? = createAudioTrack(subPcm)

    fun playClick(clickType: ClickType, volume: Float = 1.0f, isMuted: Boolean = false) {
        if (isMuted || volume <= 0f) return

        val targetTrack = when (clickType) {
            ClickType.ACCENT -> accentTrack
            ClickType.NORMAL -> normalTrack
            ClickType.SUBDIVISION -> subTrack
        } ?: return

        try {
            val clampedVol = volume.coerceIn(0.0f, 1.0f)
            targetTrack.setVolume(clampedVol)
            if (targetTrack.playState == AudioTrack.PLAYSTATE_PLAYING) {
                targetTrack.stop()
            }
            targetTrack.reloadStaticData()
            targetTrack.play()
        } catch (_: Exception) {
        }
    }

    private fun createAudioTrack(pcmData: ShortArray): AudioTrack? {
        return try {
            val bufferSize = pcmData.size * 2
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(pcmData, 0, pcmData.size)
            track
        } catch (_: Exception) {
            null
        }
    }

    private fun generateClickPcm(frequency: Double, durationMs: Double, maxVolume: Float): ShortArray {
        val numSamples = ((sampleRate * durationMs) / 1000.0).toInt()
        val pcm = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val time = i.toDouble() / sampleRate
            val envelope = exp(-12.0 * (i.toDouble() / numSamples))
            val sineValue = sin(2.0 * PI * frequency * time)
            val sampleValue = (sineValue * envelope * maxVolume * 32767.0).toInt().coerceIn(-32768, 32767)
            pcm[i] = sampleValue.toShort()
        }
        return pcm
    }

    fun release() {
        try {
            accentTrack?.stop()
            accentTrack?.release()
            normalTrack?.stop()
            normalTrack?.release()
            subTrack?.stop()
            subTrack?.release()
        } catch (_: Exception) {
        } finally {
            accentTrack = null
            normalTrack = null
            subTrack = null
        }
    }
}
