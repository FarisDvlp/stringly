package com.stringly.app.metronome.engine

import com.stringly.app.metronome.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.time.Duration.Companion.nanoseconds

class MetronomeEngine(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob()),
    private val onTick: (clickType: ClickType, volume: Float, isMuted: Boolean) -> Unit = { _, _, _ -> }
) {

    private val _state = MutableStateFlow(MetronomeState())
    val state: StateFlow<MetronomeState> = _state.asStateFlow()

    private var loopJob: Job? = null
    private var isEngineRunning = false

    fun start() {
        if (_state.value.status == MetronomeStatus.RUNNING) return

        _state.value = _state.value.copy(
            status = MetronomeStatus.RUNNING,
            currentBeat = 1,
            currentBar = 1,
            currentSubTick = 1,
            isAccentBeat = true
        )
        isEngineRunning = true
        launchEngineLoop()
    }

    fun pause() {
        if (_state.value.status != MetronomeStatus.RUNNING) return

        isEngineRunning = false
        loopJob?.cancel()
        loopJob = null
        _state.value = _state.value.copy(status = MetronomeStatus.PAUSED)
    }

    fun resume() {
        if (_state.value.status != MetronomeStatus.PAUSED) return

        _state.value = _state.value.copy(status = MetronomeStatus.RUNNING)
        isEngineRunning = true
        launchEngineLoop()
    }

    fun stop() {
        isEngineRunning = false
        loopJob?.cancel()
        loopJob = null
        _state.value = _state.value.copy(
            status = MetronomeStatus.STOPPED,
            currentBeat = 1,
            currentBar = 1,
            currentSubTick = 1,
            isAccentBeat = true
        )
    }

    fun setBpm(bpm: Int) {
        val clampedBpm = bpm.coerceIn(30, 300)
        if (_state.value.bpm == clampedBpm) return

        _state.value = _state.value.copy(
            bpm = clampedBpm,
            tempoMarking = MetronomeState.getTempoMarking(clampedBpm)
        )
    }

    fun increaseBpm(delta: Int = 1) {
        setBpm(_state.value.bpm + delta)
    }

    fun decreaseBpm(delta: Int = 1) {
        setBpm(_state.value.bpm - delta)
    }

    fun setTimeSignature(numerator: Int, denominator: Int) {
        val safeNum = numerator.coerceIn(1, 32)
        val safeDenom = if (denominator in listOf(2, 4, 8, 16)) denominator else 4
        val newTs = TimeSignature(safeNum, safeDenom)

        if (_state.value.timeSignature == newTs) return

        val currentBeat = _state.value.currentBeat.coerceIn(1, safeNum)
        _state.value = _state.value.copy(
            timeSignature = newTs,
            currentBeat = currentBeat,
            isAccentBeat = (currentBeat == 1)
        )
    }

    fun setSubdivision(subdivision: Subdivision) {
        if (_state.value.subdivision == subdivision) return

        _state.value = _state.value.copy(
            subdivision = subdivision,
            currentSubTick = 1
        )
    }

    fun setMuted(muted: Boolean) {
        _state.value = _state.value.copy(isMuted = muted)
    }

    fun setVolume(volume: Float) {
        val clampedVol = volume.coerceIn(0f, 1f)
        _state.value = _state.value.copy(volume = clampedVol)
    }

    private fun launchEngineLoop() {
        loopJob?.cancel()
        loopJob = scope.launch {
            var nextTargetNanos = System.nanoTime()

            while (isActive && isEngineRunning) {
                val currentState = _state.value
                val subTick = currentState.currentSubTick
                val beat = currentState.currentBeat
                val numerator = currentState.timeSignature.numerator
                val denominator = currentState.timeSignature.denominator
                val ticksPerBeat = currentState.subdivision.ticksPerBeat

                val clickType = when {
                    subTick == 1 && beat == 1 -> ClickType.ACCENT
                    subTick == 1 -> ClickType.NORMAL
                    else -> ClickType.SUBDIVISION
                }

                val isAccent = (clickType == ClickType.ACCENT)

                _state.value = currentState.copy(
                    isAccentBeat = isAccent
                )

                onTick(clickType, currentState.volume, currentState.isMuted)

                val baseBeatDurationNanos = (60_000_000_000.0 / (currentState.bpm * (denominator / 4.0))).toLong()
                val tickDurationNanos = baseBeatDurationNanos / ticksPerBeat

                nextTargetNanos += tickDurationNanos
                val currentNanos = System.nanoTime()
                val waitNanos = nextTargetNanos - currentNanos

                if (waitNanos > 0) {
                    delay(waitNanos.nanoseconds)
                } else {
                    nextTargetNanos = System.nanoTime()
                    yield()
                }

                if (!isEngineRunning) break

                var nextSubTick = subTick + 1
                var nextBeat = beat
                var nextBar = currentState.currentBar

                if (nextSubTick > ticksPerBeat) {
                    nextSubTick = 1
                    nextBeat++
                    if (nextBeat > numerator) {
                        nextBeat = 1
                        nextBar++
                    }
                }

                _state.value = _state.value.copy(
                    currentBeat = nextBeat,
                    currentBar = nextBar,
                    currentSubTick = nextSubTick,
                    isAccentBeat = (nextSubTick == 1 && nextBeat == 1)
                )
            }
        }
    }

    fun release() {
        stop()
        scope.cancel()
    }
}
