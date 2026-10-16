package com.misgus.pengu.window

import androidx.compose.ui.geometry.Rect

/**
 * Result of a hit test in the Windows non-client area.
 *
 * The values are the documented `HT*` constants `DefWindowProc` expects from
 * `WM_NCHITTEST`, so the native window procedure can return them unchanged. The
 * frame keeps the native window styles and erases the non-client area itself, so
 * these results are what give the window native drag, Aero Snap, resize and
 * Windows 11 Snap Layouts.
 */
enum class WinHitResult(
    val value: Int,
) {
    /** Pass the hit test to the parent window (`HTTRANSPARENT`). */
    TRANSPARENT(-1),

    /** No part of the window (`HTNOWHERE`). */
    NOWHERE(0),

    /** Client area (`HTCLIENT`). */
    CLIENT(1),

    /** Caption: native move, double-click maximize and snap (`HTCAPTION`). */
    CAPTION(2),

    /** Minimize button (`HTMINBUTTON`). */
    CAPTION_MIN(8),

    /** Maximize button: Windows 11 shows the Snap Layouts flyout (`HTMAXBUTTON`). */
    CAPTION_MAX(9),

    /** Close button (`HTCLOSE`). */
    CAPTION_CLOSE(20),

    /** Window edges the OS resizes (`HTLEFT` … `HTBOTTOMRIGHT`). */
    BORDER_LEFT(10),
    BORDER_RIGHT(11),
    BORDER_TOP(12),
    BORDER_TOP_LEFT(13),
    BORDER_TOP_RIGHT(14),
    BORDER_BOTTOM(15),
    BORDER_BOTTOM_LEFT(16),
    BORDER_BOTTOM_RIGHT(17),
}

/**
 * Geometry of the Compose-drawn title bar, read by the native window procedure.
 *
 * The values are physical pixels relative to the client area, the space
 * `WM_NCHITTEST` works in after `ScreenToClient`. The composition writes them
 * from `onSizeChanged` / `boundsInWindow`; the procedure only reads them.
 */
class WindowsWindowFrameState {
    var titleBarHeightPx: Int = 0
    var minimizeRect: Rect = Rect.Zero
    var maximizeRect: Rect = Rect.Zero
    var closeRect: Rect = Rect.Zero

    /** Resolves a client-area point to the window part the pointer is over. */
    fun hitTest(
        x: Int,
        y: Int,
    ): WinHitResult =
        when {
            minimizeRect.containsPoint(x, y) -> WinHitResult.CAPTION_MIN
            maximizeRect.containsPoint(x, y) -> WinHitResult.CAPTION_MAX
            closeRect.containsPoint(x, y) -> WinHitResult.CAPTION_CLOSE
            y < titleBarHeightPx -> WinHitResult.CAPTION
            else -> WinHitResult.CLIENT
        }

    /** Clears the geometry when the bar leaves the composition. */
    fun reset() {
        titleBarHeightPx = 0
        minimizeRect = Rect.Zero
        maximizeRect = Rect.Zero
        closeRect = Rect.Zero
    }
}

private fun Rect.containsPoint(
    x: Int,
    y: Int,
): Boolean = x >= left && x < right && y >= top && y < bottom

/**
 * Whether the app runs on Windows, the only target with the custom frame.
 *
 * macOS and Linux keep the native decoration, so every custom-frame helper is
 * gated behind this.
 */
fun isWindowsOs(): Boolean = System.getProperty("os.name").orEmpty().startsWith("Windows")
