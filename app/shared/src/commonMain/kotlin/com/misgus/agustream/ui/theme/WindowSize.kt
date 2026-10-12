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

/**
 * Width of the app window in dp.
 *
 * A surface that lives outside the shell's constraints needs it: a `Dialog`
 * opens its own window, so a fixed width there can exceed the app window.
 * Defaults to [Dp.Infinity], which leaves such a caller unconstrained.
 */
val LocalWindowWidth = staticCompositionLocalOf { Dp.Infinity }

/** Side padding for grids, rails, headers and form fields; smaller on a phone. */
val WindowSize.screenPadding: Dp
    get() = if (this == WindowSize.COMPACT) AppSpacing.md else AppSpacing.xl

/** Gap between grid cells and rail cards; tighter on a phone. */
val WindowSize.gridSpacing: Dp
    get() = if (this == WindowSize.COMPACT) AppSpacing.sm else AppSpacing.md
