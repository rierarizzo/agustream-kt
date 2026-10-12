package com.misgus.agustream.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Window width class, following Material's breakpoints.
 *
 * It is resolved once at the shell root and read through [LocalWindowSize], so a
 * screen picks a layout without measuring the window itself.
 */
enum class WindowSize {
    /** Phone portrait, or a narrow window: below 600dp. */
    COMPACT,

    /** Phone landscape, tablet portrait, or a medium window: 600dp to 839dp. */
    MEDIUM,

    /** Tablet landscape, desktop, or TV: 840dp and up. */
    EXPANDED,
    ;

    companion object {
        /** Breakpoint between [COMPACT] and [MEDIUM]. */
        val CompactMaxWidth: Dp = 600.dp

        /** Breakpoint between [MEDIUM] and [EXPANDED]. */
        val MediumMaxWidth: Dp = 840.dp

        /** Classifies [width]. */
        fun of(width: Dp): WindowSize =
            when {
                width < CompactMaxWidth -> COMPACT
                width < MediumMaxWidth -> MEDIUM
                else -> EXPANDED
            }
    }
}

/**
 * [WindowSize] of the window the app is rendering into.
 *
 * Defaults to [WindowSize.EXPANDED], so a preview or a test without the provider
 * gets the desktop layout.
 */
val LocalWindowSize = staticCompositionLocalOf { WindowSize.EXPANDED }
