package com.misgus.agustream

import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import com.misgus.agustream.ui.window.WindowController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * [WindowController] backed by a Compose Desktop [WindowState].
 *
 * The window uses its native decoration, so this routes fullscreen (the
 * placement before entering fullscreen is remembered and restored on exit) and
 * reports whether the window is maximized.
 */
class ComposeWindowController(
    private val windowState: WindowState,
    scope: CoroutineScope,
) : WindowController {
    private val _isFullScreen = MutableStateFlow(false)
    override val isFullScreen: StateFlow<Boolean> = _isFullScreen.asStateFlow()

    private val _isMaximized = MutableStateFlow(windowState.placement == WindowPlacement.Maximized)
    override val isMaximized: StateFlow<Boolean> = _isMaximized.asStateFlow()

    private var previousPlacement: WindowPlacement = WindowPlacement.Floating

    init {
        scope.launch {
            snapshotFlow { windowState.placement }
                .collect { _isMaximized.value = it == WindowPlacement.Maximized }
        }
    }

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
