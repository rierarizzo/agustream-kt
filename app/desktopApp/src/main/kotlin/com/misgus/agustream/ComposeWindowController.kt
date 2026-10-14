package com.misgus.agustream

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import com.misgus.agustream.ui.window.WindowController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.awt.GraphicsEnvironment

/**
 * [WindowController] backed by a Compose Desktop [WindowState].
 *
 * The window uses its native decoration, so this routes fullscreen (the
 * placement before entering fullscreen is remembered and restored on exit) and
 * reports the screen work area the Home billboard is sized from.
 */
class ComposeWindowController(
    private val windowState: WindowState,
) : WindowController {
    private val _isFullScreen = MutableStateFlow(false)
    override val isFullScreen: StateFlow<Boolean> = _isFullScreen.asStateFlow()

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

    override fun setFullScreen(value: Boolean) {
        if (value) {
            previousPlacement = windowState.placement
                .takeIf { it != WindowPlacement.Fullscreen }
                ?: WindowPlacement.Floating
            windowState.placement = WindowPlacement.Fullscreen
        } else {
            windowState.placement = previousPlacement
        }
        _isFullScreen.value = value
    }
}
