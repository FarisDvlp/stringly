package com.stringly.app.metronome.controller

import com.stringly.app.metronome.audio.MetronomeAudioPlayer
import com.stringly.app.metronome.engine.MetronomeEngine
import com.stringly.app.metronome.model.MetronomeState
import com.stringly.app.metronome.model.Subdivision
import com.stringly.app.metronome.taptempo.TapTempoCalculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow

class MetronomeController(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) {
    private val audioPlayer = MetronomeAudioPlayer()
    private val tapTempoCalculator = TapTempoCalculator()

    private val engine = MetronomeEngine(
        scope = scope,
        onTick = { clickType, volume, isMuted ->
            audioPlayer.playClick(clickType, volume, isMuted)
        }
    )

    val state: StateFlow<MetronomeState> = engine.state

    fun start() {
        engine.start()
    }

    fun pause() {
        engine.pause()
    }

    fun resume() {
        engine.resume()
    }

    fun stop() {
        engine.stop()
    }

    fun setBpm(bpm: Int) {
        engine.setBpm(bpm)
    }

    fun increaseBpm(delta: Int = 1) {
        engine.increaseBpm(delta)
    }

    fun decreaseBpm(delta: Int = 1) {
        engine.decreaseBpm(delta)
    }

    fun setTimeSignature(numerator: Int, denominator: Int) {
        engine.setTimeSignature(numerator, denominator)
    }

    fun setSubdivision(subdivision: Subdivision) {
        engine.setSubdivision(subdivision)
    }

    fun setMuted(muted: Boolean) {
        engine.setMuted(muted)
    }

    fun setVolume(volume: Float) {
        engine.setVolume(volume)
    }

    fun tapTempo(): Int? {
        val calculatedBpm = tapTempoCalculator.tap()
        if (calculatedBpm != null) {
            setBpm(calculatedBpm)
        }
        return calculatedBpm
    }

    fun resetTapTempo() {
        tapTempoCalculator.reset()
    }

    fun release() {
        engine.release()
        audioPlayer.release()
    }
}
