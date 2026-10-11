package com.misgus.agustream.ui.shell

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

/**
 * The colour that tints the app background, derived from the artwork on screen.
 *
 * Screens register a colour through [AmbientBackground]; this holder only stores
 * the current value and remembers which screen owns it, so a screen being
 * disposed cannot clear the tint already set by the screen replacing it.
 */
class AppAmbient {
    private var owner: Any? = null

    var color: Color? by mutableStateOf(null)
        private set

    /** Applies [color] on behalf of [owner]. */
    fun set(
        owner: Any,
        color: Color,
    ) {
        this.owner = owner
        this.color = color
    }

    /** Clears the tint, but only when [owner] is still the one that set it. */
    fun clear(owner: Any) {
        if (this.owner === owner) {
            this.owner = null
            this.color = null
        }
    }
}
