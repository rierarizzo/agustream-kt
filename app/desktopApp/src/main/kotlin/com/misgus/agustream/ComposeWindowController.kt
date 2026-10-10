package com.misgus.agustream

import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import com.misgus.agustream.ui.window.WindowController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * [WindowController] backed by a Compose Desktop [WindowState].
 *
 * The window uses its native decoration, so this only routes fullscreen: the
 * placement before entering fullscreen is remembered and restored on exit.
 */
class ComposeWindowController(
    private val windowState: WindowState,
) : WindowController {
    private val _isFullScreen = MutableStateFlow(false)
    override val isFullScreen: StateFlow<Boolean> = _isFullScreen.asStateFlow()

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
