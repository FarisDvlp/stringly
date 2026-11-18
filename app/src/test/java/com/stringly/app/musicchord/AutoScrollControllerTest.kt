package com.stringly.app.musicchord

import com.stringly.app.musicchord.autoscroll.AutoScrollController
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AutoScrollControllerTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Test
    fun testAutoScrollStartPauseResumeStop() = testScope.runTest {
        val controller = AutoScrollController(scope = testScope)

        assertFalse(controller.uiState.value.isScrolling)
        assertFalse(controller.uiState.value.isPaused)

        // Start
        controller.start()
        assertTrue(controller.uiState.value.isScrolling)
        assertFalse(controller.uiState.value.isPaused)

        // Pause
        controller.pause()
        assertFalse(controller.uiState.value.isScrolling)
        assertTrue(controller.uiState.value.isPaused)

        // Resume
        controller.resume()
        assertTrue(controller.uiState.value.isScrolling)
        assertFalse(controller.uiState.value.isPaused)

        // Stop
        controller.stop()
        assertFalse(controller.uiState.value.isScrolling)
        assertFalse(controller.uiState.value.isPaused)
        assertEquals(0f, controller.uiState.value.currentScrollOffset, 0.001f)
    }

    @Test
    fun testAutoScrollSpeedMultiplier() {
        val controller = AutoScrollController(scope = testScope)

        controller.setSpeed(1.5f)
        assertEquals(1.5f, controller.uiState.value.speedMultiplier, 0.001f)

        controller.setSpeed(2.0f)
        assertEquals(2.0f, controller.uiState.value.speedMultiplier, 0.001f)

        controller.setSpeed(0.5f)
        assertEquals(0.5f, controller.uiState.value.speedMultiplier, 0.001f)
    }
}
