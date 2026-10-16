package com.misgus.pengu.window

import androidx.compose.ui.geometry.Rect
import kotlin.test.Test
import kotlin.test.assertEquals

class WindowsWindowFrameStateTest {
    @Test
    fun resolvesTheCaptionButtons() {
        val state = frameState()

        assertEquals(WinHitResult.CAPTION_MIN, state.hitTest(20, 10))
        assertEquals(WinHitResult.CAPTION_MAX, state.hitTest(70, 10))
        assertEquals(WinHitResult.CAPTION_CLOSE, state.hitTest(120, 10))
    }

    @Test
    fun resolvesTheCaptionAndTheClientArea() {
        val state = frameState()

        assertEquals(WinHitResult.CAPTION, state.hitTest(500, 20))
        assertEquals(WinHitResult.CLIENT, state.hitTest(500, 60))
    }

    @Test
    fun resetClearsTheCaptionArea() {
        val state = frameState()

        state.reset()

        assertEquals(WinHitResult.CLIENT, state.hitTest(500, 5))
        assertEquals(WinHitResult.CLIENT, state.hitTest(20, 10))
    }

    private fun frameState() =
        WindowsWindowFrameState().apply {
            titleBarHeightPx = 40
            minimizeRect = Rect(10f, 4f, 56f, 36f)
            maximizeRect = Rect(56f, 4f, 102f, 36f)
            closeRect = Rect(102f, 4f, 148f, 36f)
        }
}
