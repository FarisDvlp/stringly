package com.stringly.app.musicchord.autoscroll

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.time.Duration.Companion.milliseconds

data class AutoScrollState(
    val isScrolling: Boolean = false,
    val isPaused: Boolean = false,
    val speedMultiplier: Float = 1.0f,
    val currentScrollOffset: Float = 0f
)

class AutoScrollController(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob()),
    private val basePixelsPerSecond: Float = 40.0f
) {
    private val _uiState = MutableStateFlow(AutoScrollState())
    val uiState: StateFlow<AutoScrollState> = _uiState.asStateFlow()

    private val _scrollDeltaFlow = MutableSharedFlow<Float>(extraBufferCapacity = 64)
    val scrollDeltaFlow: SharedFlow<Float> = _scrollDeltaFlow.asSharedFlow()

    private var scrollJob: Job? = null

    fun start() {
        if (_uiState.value.isScrolling && !_uiState.value.isPaused) return

        _uiState.value = _uiState.value.copy(
            isScrolling = true,
            isPaused = false
        )
        launchScrollLoop()
    }

    fun pause() {
        if (!_uiState.value.isScrolling || _uiState.value.isPaused) return

        _uiState.value = _uiState.value.copy(
            isScrolling = false,
            isPaused = true
        )
        scrollJob?.cancel()
        scrollJob = null
    }

    fun resume() {
        if (!_uiState.value.isPaused) return

        _uiState.value = _uiState.value.copy(
            isScrolling = true,
            isPaused = false
        )
        launchScrollLoop()
    }

    fun stop() {
        scrollJob?.cancel()
        scrollJob = null
        _uiState.value = _uiState.value.copy(
            isScrolling = false,
            isPaused = false,
            currentScrollOffset = 0f
        )
    }

    fun setSpeed(multiplier: Float) {
        val validMultiplier = multiplier.coerceIn(0.25f, 4.0f)
        _uiState.value = _uiState.value.copy(
            speedMultiplier = validMultiplier
        )
    }

    fun updateScrollOffset(offset: Float) {
        _uiState.value = _uiState.value.copy(
            currentScrollOffset = offset
        )
    }

    private fun launchScrollLoop() {
        scrollJob?.cancel()
        scrollJob = scope.launch {
            val frameTimeMs = 16.milliseconds
            while (isActive && _uiState.value.isScrolling && !_uiState.value.isPaused) {
                val speed = _uiState.value.speedMultiplier
                val deltaPixels = basePixelsPerSecond * speed * 0.016f

                val newOffset = _uiState.value.currentScrollOffset + deltaPixels
                _uiState.value = _uiState.value.copy(currentScrollOffset = newOffset)
                _scrollDeltaFlow.emit(deltaPixels)

                delay(frameTimeMs)
            }
        }
    }

    fun release() {
        stop()
        scope.cancel()
    }
}
