package com.stringly.app.tuner

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs
import kotlin.time.Duration.Companion.milliseconds

data class TunerUiState(
    val permissionGranted: Boolean = false,
    val isListening: Boolean = true,
    val hasSignal: Boolean = false,
    val detectedFrequency: Float = 0f,
    val targetFrequency: Float = 82.41f,
    val noteName: String = "E",
    val octave: Int = 2,
    val centsOffset: Float = 0f,
    val status: TuningStatus = TuningStatus.LISTENING,
    val signalStrength: Float = 0f,
    val waveformPoints: List<Float> = emptyList(),
    val selectedStringIndex: Int = -1, // -1 for AUTO mode, 0..5 for standard strings
    val isAutoMode: Boolean = true,
    val errorMessage: String? = null,
)

class GuitarTunerEngine(private val context: Context) {

    private val _uiState = MutableStateFlow(TunerUiState())
    val uiState: StateFlow<TunerUiState> = _uiState.asStateFlow()

    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var recordingJob: Job? = null
    private val engineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val pitchHistory = ArrayDeque<Float>()
    private val maxHistorySize = 5
    private var prevSmoothedFreq = 0f

    fun checkPermission(): Boolean {
        val granted = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        _uiState.value = _uiState.value.copy(
            permissionGranted = granted,
            status = if (!granted) TuningStatus.NO_PERMISSION else _uiState.value.status
        )
        return granted
    }

    fun setSelectedStringIndex(index: Int) {
        val isAuto = (index < 0) || (index >= GuitarTunerConstants.STANDARD_STRINGS.size)
        val targetFreq = if (!isAuto) {
            GuitarTunerConstants.STANDARD_STRINGS[index].frequency
        } else {
            _uiState.value.targetFrequency
        }
        val targetNote = if (!isAuto) {
            GuitarTunerConstants.STANDARD_STRINGS[index].name
        } else {
            _uiState.value.noteName
        }
        val targetOctave = if (!isAuto) {
            GuitarTunerConstants.STANDARD_STRINGS[index].octave
        } else {
            _uiState.value.octave
        }

        _uiState.value = _uiState.value.copy(
            selectedStringIndex = if (isAuto) -1 else index,
            isAutoMode = isAuto,
            targetFrequency = targetFreq,
            noteName = if (!isAuto) targetNote else _uiState.value.noteName,
            octave = if (!isAuto) targetOctave else _uiState.value.octave
        )
    }

    @SuppressLint("MissingPermission")
    fun startTuning() {
        if (!checkPermission()) {
            _uiState.value = _uiState.value.copy(
                permissionGranted = false,
                status = TuningStatus.NO_PERMISSION
            )
            return
        }

        if (isRecording) return

        recordingJob?.cancel()
        recordingJob = engineScope.launch {
            runAudioLoop()
        }
    }

    fun stopTuning() {
        isRecording = false
        recordingJob?.cancel()
        recordingJob = null
        releaseAudioRecord()
        _uiState.value = _uiState.value.copy(
            isListening = false,
            hasSignal = false,
            status = TuningStatus.LISTENING,
            signalStrength = 0f,
            waveformPoints = emptyList()
        )
    }

    private suspend fun runAudioLoop() = withContext(Dispatchers.Default) {
        val sampleRates = intArrayOf(44100, 48000)
        var selectedSampleRate = 44100
        var minBufferSize = -1

        for (rate in sampleRates) {
            val size = AudioRecord.getMinBufferSize(
                rate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            if (size > 0) {
                selectedSampleRate = rate
                minBufferSize = size
                break
            }
        }

        if (minBufferSize <= 0) {
            _uiState.value = _uiState.value.copy(
                status = TuningStatus.ERROR,
                errorMessage = "Audio recording non-supported on this device."
            )
            return@withContext
        }

        val bufferSizeShorts = maxOf(4096, minBufferSize * 2)

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                selectedSampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSizeShorts * 2
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                _uiState.value = _uiState.value.copy(
                    status = TuningStatus.ERROR,
                    errorMessage = "Failed to initialize microphone input."
                )
                releaseAudioRecord()
                return@withContext
            }

            val detector = YinPitchDetector(selectedSampleRate, bufferSizeShorts)
            val audioBuffer = ShortArray(bufferSizeShorts)

            audioRecord?.startRecording()
            isRecording = true

            _uiState.value = _uiState.value.copy(
                isListening = true,
                errorMessage = null
            )

            val waveformSampleCount = 32

            while (isRecording && isActive) {
                val readSize = audioRecord?.read(audioBuffer, 0, bufferSizeShorts) ?: 0
                if (readSize > 0) {
                    val pitchResult = detector.detectPitch(audioBuffer, readSize)

                    val step = (readSize / waveformSampleCount).coerceAtLeast(1)
                    val points = ArrayList<Float>(waveformSampleCount)
                    for (i in 0 until readSize step step) {
                        if (points.size >= waveformSampleCount) break
                        points.add((audioBuffer[i] / 32768.0f).coerceIn(-1.0f, 1.0f))
                    }

                    val signalStrength = (pitchResult.rms * 10f).coerceIn(0f, 1f)

                    if (pitchResult.isPitchDetected && pitchResult.frequency > 0f) {
                        val smoothedFreq = smoothPitch(pitchResult.frequency)

                        val currentState = _uiState.value
                        val targetString = if (currentState.isAutoMode) {
                            GuitarTunerConstants.findNearestString(smoothedFreq)
                        } else {
                            val selectedIdx = currentState.selectedStringIndex.coerceIn(0, 5)
                            GuitarTunerConstants.STANDARD_STRINGS[selectedIdx]
                        }

                        val targetFreq = targetString.frequency
                        val noteName = targetString.name
                        val octave = targetString.octave

                        val cents = GuitarTunerConstants.calculateCents(smoothedFreq, targetFreq)
                            .coerceIn(-50f, 50f)

                        val tuningStatus = when {
                            abs(cents) <= GuitarTunerConstants.IN_TUNE_TOLERANCE_CENTS -> TuningStatus.IN_TUNE
                            cents < -GuitarTunerConstants.IN_TUNE_TOLERANCE_CENTS -> TuningStatus.FLAT
                            else -> TuningStatus.SHARP
                        }

                        _uiState.value = currentState.copy(
                            isListening = true,
                            hasSignal = true,
                            detectedFrequency = smoothedFreq,
                            targetFrequency = targetFreq,
                            noteName = noteName,
                            octave = octave,
                            centsOffset = cents,
                            status = tuningStatus,
                            signalStrength = signalStrength,
                            waveformPoints = points
                        )
                    } else {
                        pitchHistory.clear()
                        prevSmoothedFreq = 0f

                        val currentState = _uiState.value
                        _uiState.value = currentState.copy(
                            isListening = true,
                            hasSignal = false,
                            status = TuningStatus.LISTENING,
                            signalStrength = signalStrength,
                            waveformPoints = points
                        )
                    }
                }
                delay(20.milliseconds)
            }
        } catch (_: SecurityException) {
            _uiState.value = _uiState.value.copy(
                permissionGranted = false,
                status = TuningStatus.NO_PERMISSION,
                errorMessage = "Microphone permission required."
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                status = TuningStatus.ERROR,
                errorMessage = e.localizedMessage ?: "Audio record error."
            )
        } finally {
            releaseAudioRecord()
        }
    }

    private fun smoothPitch(rawFreq: Float): Float {
        pitchHistory.addLast(rawFreq)
        if (pitchHistory.size > maxHistorySize) {
            pitchHistory.removeFirst()
        }

        val sorted = pitchHistory.sorted()
        val medianFreq = sorted[sorted.size / 2]

        if (prevSmoothedFreq == 0f) {
            prevSmoothedFreq = medianFreq
            return medianFreq
        }

        val alpha = 0.35f
        val smoothed = alpha * medianFreq + (1f - alpha) * prevSmoothedFreq
        prevSmoothedFreq = smoothed
        return smoothed
    }

    private fun releaseAudioRecord() {
        try {
            if (audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                audioRecord?.stop()
            }
            audioRecord?.release()
        } catch (_: Exception) {
        } finally {
            audioRecord = null
            isRecording = false
        }
    }

    fun release() {
        stopTuning()
        engineScope.cancel()
    }
}
