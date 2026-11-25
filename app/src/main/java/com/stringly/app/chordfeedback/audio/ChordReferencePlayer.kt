package com.stringly.app.chordfeedback.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import com.stringly.app.chordfeedback.model.ChordData
import java.io.File
import java.io.FileOutputStream

class ChordReferencePlayer(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private var audioTrack: AudioTrack? = null
    private var isPlaying = false

    fun isPlayingReference(): Boolean = isPlaying

    fun playReference(chordData: ChordData) {
        stop()

        try {
            // Attempt playing from assets first
            val assetFileName = chordData.audioAssetName
            val afd = try {
                context.assets.openFd(assetFileName)
            } catch (_: Exception) {
                null
            }

            if (afd != null) {
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    afd.close()
                    setOnCompletionListener {
                        this@ChordReferencePlayer.isPlaying = false
                    }
                    prepare()
                    start()
                }
                isPlaying = true
            } else {
                // Fallback: Check local cache file or play generated PCM AudioTrack
                val cacheDir = File(context.cacheDir, "chords")
                if (!cacheDir.exists()) cacheDir.mkdirs()

                val fileName = chordData.audioAssetName.substringAfterLast("/")
                val cacheFile = File(cacheDir, fileName)

                if (!cacheFile.exists() || cacheFile.length() < 100) {
                    val wavData = ChordWavAssetHelper.generateChordWavData(chordData)
                    FileOutputStream(cacheFile).use { it.write(wavData) }
                }

                if (cacheFile.exists()) {
                    mediaPlayer = MediaPlayer().apply {
                        setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build()
                        )
                        setDataSource(cacheFile.absolutePath)
                        setOnCompletionListener {
                            this@ChordReferencePlayer.isPlaying = false
                        }
                        prepare()
                        start()
                    }
                    isPlaying = true
                } else {
                    // Fallback to direct AudioTrack static PCM playback
                    playPcmDirect(chordData)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            playPcmDirect(chordData)
        }
    }

    private fun playPcmDirect(chordData: ChordData) {
        try {
            val wavData = ChordWavAssetHelper.generateChordWavData(chordData)
            // Skip 44-byte WAV header to get raw 16-bit PCM bytes
            val pcmBytes = if (wavData.size > 44) wavData.copyOfRange(44, wavData.size) else wavData
            val sampleRate = 44100
            val bufferSize = pcmBytes.size

            audioTrack = AudioTrack.Builder()
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

            audioTrack?.write(pcmBytes, 0, pcmBytes.size)
            audioTrack?.play()
            isPlaying = true
        } catch (e: Exception) {
            e.printStackTrace()
            isPlaying = false
        }
    }

    fun stop() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop()
            }
            mediaPlayer?.release()
        } catch (_: Exception) {
        } finally {
            mediaPlayer = null
        }

        try {
            if (audioTrack?.playState == AudioTrack.PLAYSTATE_PLAYING) {
                audioTrack?.stop()
            }
            audioTrack?.release()
        } catch (_: Exception) {
        } finally {
            audioTrack = null
            isPlaying = false
        }
    }

    fun release() {
        stop()
    }
}
