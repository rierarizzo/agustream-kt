package com.misgus.agustream

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.platform.win32.WinDef.HWND
import com.sun.jna.ptr.IntByReference
import java.awt.Color
import java.awt.Window

/**
 * Paints the native window decoration to match the app.
 *
 * AWT windows keep the light system title bar until the app opts into the DWM
 * immersive dark mode; Windows 11 additionally lets the app set explicit
 * caption, text and border colours (attributes 35, 36 and 34) and the corner
 * rounding (attribute 33), while Windows 10 only supports the light/dark flag.
 * The app keeps the native decoration on purpose (see the architecture
 * decisions), so this is the only place that talks to DWM.
 *
 * Returns `true` once the request reached the window, or `false` when the native
 * peer does not exist yet, so the caller can retry on the next frame.
 */
fun applyDarkTitleBar(
    window: Window,
    caption: Color? = null,
    text: Color? = null,
    border: Color? = null,
): Boolean {
    if (!isWindows()) return true
    val hwnd =
        runCatching { Native.getWindowPointer(window) }
            .getOrNull()
            ?.let { HWND(it) }
            ?: return false

    // Immersive dark mode, so the caption buttons match a dark title bar.
    val enabled = IntByReference(1)
    if (DwmApi.INSTANCE.DwmSetWindowAttribute(hwnd, DWMWA_USE_IMMERSIVE_DARK_MODE, enabled, INT_SIZE) != 0) {
        DwmApi.INSTANCE.DwmSetWindowAttribute(hwnd, DWMWA_USE_IMMERSIVE_DARK_MODE_OLD, enabled, INT_SIZE)
    }
    // Explicit colours; Windows 11 only, ignored on older builds.
    caption?.let { setColor(hwnd, DWMWA_CAPTION_COLOR, it) }
    text?.let { setColor(hwnd, DWMWA_TEXT_COLOR, it) }
    border?.let { setColor(hwnd, DWMWA_BORDER_COLOR, it) }
    // Rounded corners; Windows 11 only, ignored on older builds.
    val corner = IntByReference(DWMWCP_ROUND)
    DwmApi.INSTANCE.DwmSetWindowAttribute(hwnd, DWMWA_WINDOW_CORNER_PREFERENCE, corner, INT_SIZE)
    return true
}

private fun setColor(
    hwnd: HWND,
    attribute: Int,
    color: Color,
) {
    val value = IntByReference(color.toColorRef())
    DwmApi.INSTANCE.DwmSetWindowAttribute(hwnd, attribute, value, INT_SIZE)
}

/** Windows COLORREF is `0x00BBGGRR`, the reverse of the usual RGB order. */
private fun Color.toColorRef(): Int = (blue shl 16) or (green shl 8) or red

/** Bytes in an `Int` attribute value. */
private const val INT_SIZE = 4

private const val DWMWA_USE_IMMERSIVE_DARK_MODE = 20
private const val DWMWA_USE_IMMERSIVE_DARK_MODE_OLD = 19
private const val DWMWA_WINDOW_CORNER_PREFERENCE = 33
private const val DWMWA_BORDER_COLOR = 34
private const val DWMWA_CAPTION_COLOR = 35
private const val DWMWA_TEXT_COLOR = 36

/** `DWMWCP_ROUND`: the standard rounded corners. */
private const val DWMWCP_ROUND = 2

private fun isWindows(): Boolean = System.getProperty("os.name").startsWith("Windows")

/** The DWM entry points this app needs. */
private interface DwmApi : Library {
    // Matches the exported Win32 symbol, so the naming rule does not apply.
    @Suppress("ktlint:standard:function-naming")
    fun DwmSetWindowAttribute(
        hwnd: HWND,
        attribute: Int,
        value: IntByReference,
        size: Int,
    ): Int

    companion object {
        val INSTANCE: DwmApi = Native.load("dwmapi", DwmApi::class.java)
    }
}
