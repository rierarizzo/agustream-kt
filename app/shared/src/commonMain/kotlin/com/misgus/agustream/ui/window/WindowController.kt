package com.misgus.agustream.ui.window

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Controls the parts of the OS window the app needs to drive itself.
 *
 * The window keeps its **native decoration** (title bar, minimize, maximize,
 * close and resizing are handled by the OS). The only behaviour routed through
 * this interface is fullscreen, because the player owns that toggle.
 */
interface WindowController {
    val isFullScreen: StateFlow<Boolean>

    /**
     * Whether the OS window is maximized. Always `true` where the concept does
     * not exist (a phone window fills the screen).
     */
    val isMaximized: StateFlow<Boolean>

    fun setFullScreen(value: Boolean)
}

/** [WindowController] that does nothing; used by previews and tests. */
class NoopWindowController : WindowController {
    private val _isFullScreen = MutableStateFlow(false)
    override val isFullScreen: StateFlow<Boolean> = _isFullScreen.asStateFlow()

    private val _isMaximized = MutableStateFlow(true)
    override val isMaximized: StateFlow<Boolean> = _isMaximized.asStateFlow()

    override fun setFullScreen(value: Boolean) {
        _isFullScreen.value = value
    }
}
