package com.misgus.agustream.ui.shell

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * App-level chrome state the shell reacts to.
 *
 * The player asks for immersive mode while it is open, so the navigation rail
 * is hidden even when the window is not fullscreen (the title bar stays, so the
 * window can still be moved or closed).
 */
class AppChrome {
    var immersive by mutableStateOf(false)
        private set

    private var requests = 0

    /**
     * Hides the chrome until the matching [exitImmersive].
     *
     * Counted, so when a player replaces another one (autoplay) the chrome does
     * not flash back during the transition.
     */
    fun enterImmersive() {
        requests++
        immersive = true
    }

    /** Releases one immersive request. */
    fun exitImmersive() {
        if (requests > 0) requests--
        immersive = requests > 0
    }
}
