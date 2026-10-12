package com.misgus.agustream

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.platform.win32.WinDef.HWND
import com.sun.jna.ptr.IntByReference
import java.awt.Window

/**
 * Asks Windows to paint the native title bar dark.
 *
 * AWT windows keep the light system title bar until the app opts into the DWM
 * immersive dark mode, so the bar stays light even when the OS is in dark mode.
 * The app keeps the native decoration on purpose (see the architecture
 * decisions), so this is the only place that talks to DWM.
 *
 * Returns `true` once the request reached the window, or `false` when the native
 * peer does not exist yet, so the caller can retry on the next frame.
 */
fun applyDarkTitleBar(window: Window): Boolean {
    if (!isWindows()) return true
    val hwnd =
        runCatching { Native.getWindowPointer(window) }
            .getOrNull()
            ?.let { HWND(it) }
            ?: return false
    val enabled = IntByReference(1)
    // DWMWA_USE_IMMERSIVE_DARK_MODE is 20 since Windows 10 20H1; earlier builds used 19.
    val result = DwmApi.INSTANCE.DwmSetWindowAttribute(hwnd, 20, enabled, ENABLE_SIZE)
    if (result != 0) {
        DwmApi.INSTANCE.DwmSetWindowAttribute(hwnd, 19, enabled, ENABLE_SIZE)
    }
    return true
}

/** Bytes in an `Int` attribute value. */
private const val ENABLE_SIZE = 4

private fun isWindows(): Boolean = System.getProperty("os.name").startsWith("Windows")

/** The one DWM entry point this app needs. */
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
