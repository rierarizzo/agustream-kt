package com.misgus.pengu

import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import com.misgus.pengu.ui.window.WindowController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.awt.GraphicsEnvironment
import javax.swing.SwingUtilities

/**
 * [WindowController] backed by a Compose Desktop [WindowState].
 *
 * The window uses its native decoration, so this routes fullscreen (the
 * placement before entering fullscreen is remembered and restored on exit) and
 * reports the screen work area the Home billboard is sized from.
 *
 * Fullscreen is driven through the native [ComposeWindow] rather than only
 * through [WindowState.placement] because Compose's `Maximized` transition never
 * clears its fullscreen bit, and exclusive fullscreen clears the maximize bit on
 * entry without restoring it on exit. The window is therefore taken out of
 * fullscreen through `Floating` first and its previous placement is re-applied
 * afterwards. These are workarounds for Compose Desktop / AWT behaviour and can
 * be simplified if it is fixed upstream.
 */
class ComposeWindowController(
    private val windowState: WindowState,
) : WindowController {
    private val _isFullScreen = MutableStateFlow(false)
    override val isFullScreen: StateFlow<Boolean> = _isFullScreen.asStateFlow()

    override val supportsFullScreen: Boolean = true

    // AWT reports the work area in the same logical (density-independent) space
    // Compose uses, so the height is a dp value without a density conversion.
    override val screenWorkAreaHeight: Dp =
        runCatching {
            GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .maximumWindowBounds
                .height
                .dp
        }.getOrDefault(0.dp)

    private var previousPlacement: WindowPlacement = WindowPlacement.Floating

    private var window: ComposeWindow? = null

    /** Binds the native window once the composition has created it. */
    fun attach(window: ComposeWindow) {
        this.window = window
    }

    override fun setFullScreen(value: Boolean) {
        val native = window ?: return
        if (value == _isFullScreen.value) return
        val device = native.graphicsConfiguration.device
        if (value) {
            previousPlacement =
                windowState.placement
                    .takeIf { it != WindowPlacement.Fullscreen }
                    ?: WindowPlacement.Floating
            native.placement = WindowPlacement.Fullscreen
            if (device.isFullScreenSupported) {
                device.fullScreenWindow = native
            }
            windowState.placement = WindowPlacement.Fullscreen
        } else {
            // Exclusive fullscreen clears the maximize bit on entry and restores
            // the (unmaximized) bounds on exit, so the previous placement is
            // re-applied by hand.
            if (device.fullScreenWindow === native) {
                device.fullScreenWindow = null
            }
            native.placement = WindowPlacement.Floating
            // Workaround: exclusive fullscreen restores the window bounds without
            // re-laying out the Compose scene, so the controls keep the fullscreen
            // size until the window is resized by hand. Nudge the size by a pixel
            // and back, in two dispatches so the intermediate layout actually
            // runs. It must happen before re-maximizing, because `setSize` on a
            // maximized frame unmaximizes it.
            val width = native.width
            val height = native.height
            native.setSize(width, height + 1)
            SwingUtilities.invokeLater {
                native.setSize(width, height)
                native.placement = previousPlacement
                windowState.placement = previousPlacement
            }
        }
        _isFullScreen.value = value
    }
}
