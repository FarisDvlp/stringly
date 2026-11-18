package com.stringly.app.metronome

import com.stringly.app.metronome.engine.MetronomeEngine
import com.stringly.app.metronome.model.*
import com.stringly.app.metronome.taptempo.TapTempoCalculator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MetronomeUnitTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Test
    fun testBpmClampingAndStepping() {
        val engine = MetronomeEngine(scope = testScope)

        engine.setBpm(100)
        assertEquals(100, engine.state.value.bpm)
        assertEquals("Andante", engine.state.value.tempoMarking)

        engine.setBpm(10) // Below min -> clamped to 30
        assertEquals(30, engine.state.value.bpm)
        assertEquals("Largo", engine.state.value.tempoMarking)

        engine.setBpm(500) // Above max -> clamped to 300
        assertEquals(300, engine.state.value.bpm)
        assertEquals("Prestissimo", engine.state.value.tempoMarking)

        engine.setBpm(120)
        engine.increaseBpm(5)
        assertEquals(125, engine.state.value.bpm)

        engine.decreaseBpm(10)
        assertEquals(115, engine.state.value.bpm)
    }

    @Test
    fun testTimeSignatures() {
        val engine = MetronomeEngine(scope = testScope)

        engine.setTimeSignature(2, 4)
        assertEquals(2, engine.state.value.timeSignature.numerator)
        assertEquals(4, engine.state.value.timeSignature.denominator)
        assertEquals("2/4", engine.state.value.timeSignature.label)

        engine.setTimeSignature(3, 4)
        assertEquals("3/4", engine.state.value.timeSignature.label)

        engine.setTimeSignature(6, 8)
        assertEquals("6/8", engine.state.value.timeSignature.label)
    }

    @Test
    fun testControllerStateTransitions() = testScope.runTest {
        val engine = MetronomeEngine(scope = testScope)

        assertEquals(MetronomeStatus.STOPPED, engine.state.value.status)

        engine.start()
        assertEquals(MetronomeStatus.RUNNING, engine.state.value.status)
        assertEquals(1, engine.state.value.currentBeat)
        assertTrue(engine.state.value.isAccentBeat)

        engine.pause()
        assertEquals(MetronomeStatus.PAUSED, engine.state.value.status)

        engine.resume()
        assertEquals(MetronomeStatus.RUNNING, engine.state.value.status)

        engine.stop()
        assertEquals(MetronomeStatus.STOPPED, engine.state.value.status)
        assertEquals(1, engine.state.value.currentBeat)
    }

    @Test
    fun testTapTempoCalculator() {
        val tapCalculator = TapTempoCalculator(timeoutMs = 2500L)

        val now = 10000L
        // First tap -> null
        assertNull(tapCalculator.tap(now))

        // Second tap after 500ms (120 BPM = 500ms per beat)
        val bpm1 = tapCalculator.tap(now + 500)
        assertEquals(120, bpm1)

        // Third tap after another 500ms
        val bpm2 = tapCalculator.tap(now + 1000)
        assertEquals(120, bpm2)

        // Fourth tap after long delay (3000ms > timeout 2500ms) -> resets history and returns null
        val bpmReset = tapCalculator.tap(now + 4000)
        assertNull(bpmReset)

        // Tap again after 600ms (100 BPM)
        val bpmNew = tapCalculator.tap(now + 4600)
        assertEquals(100, bpmNew)
    }

    @Test
    fun testSubdivisionSettings() {
        val engine = MetronomeEngine(scope = testScope)

        engine.setSubdivision(Subdivision.EIGHTH)
        assertEquals(Subdivision.EIGHTH, engine.state.value.subdivision)
        assertEquals(2, engine.state.value.subdivision.ticksPerBeat)

        engine.setSubdivision(Subdivision.SIXTEENTH)
        assertEquals(Subdivision.SIXTEENTH, engine.state.value.subdivision)
        assertEquals(4, engine.state.value.subdivision.ticksPerBeat)
    }
}
