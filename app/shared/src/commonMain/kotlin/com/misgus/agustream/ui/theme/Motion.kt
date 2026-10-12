package com.misgus.agustream.ui.theme

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Whether the app plays motion.
 *
 * Every animation reads it (through [motionTween] or by checking the value
 * directly), so a future "disable animations" setting only has to flip this at
 * the root. When it is `false`, specs collapse to a zero duration and Compose
 * snaps to the target on the next frame.
 */
val LocalMotionEnabled = staticCompositionLocalOf { true }

/**
 * Motion design tokens.
 *
 * Durations follow Material's short/medium/long scale; all motion in the app
 * should come from here instead of hardcoding values per screen, so the
 * [LocalMotionEnabled] switch stays a one-line change.
 */
object AppMotion {
    /** Hover, press and other immediate feedback. */
    const val FAST = 120

    /** Most transitions: fades, expanding rows, dialog entrances. */
    const val MEDIUM = 220

    /** Screen-level transitions and larger surfaces. */
    const val SLOW = 340

    val standard: Easing = FastOutSlowInEasing

    /** A tween that snaps to the target when [enabled] is `false`. */
    fun <T> spec(
        enabled: Boolean,
        durationMillis: Int = MEDIUM,
        delayMillis: Int = 0,
        easing: Easing = standard,
    ): TweenSpec<T> =
        tween(
            durationMillis = if (enabled) durationMillis else 0,
            delayMillis = if (enabled) delayMillis else 0,
            easing = easing,
        )
}

/** [AppMotion.spec] bound to the current [LocalMotionEnabled]. */
@Composable
fun <T> motionTween(
    durationMillis: Int = AppMotion.MEDIUM,
    delayMillis: Int = 0,
    easing: Easing = AppMotion.standard,
): TweenSpec<T> = AppMotion.spec(LocalMotionEnabled.current, durationMillis, delayMillis, easing)

/**
 * Applies [scale] and [alpha] in the draw phase.
 *
 * `graphicsLayer` transforms the layer without relayout and without recomposing
 * per frame. The values are read from the lambdas inside the layer block (the
 * draw phase), so callers should pass `{ state.value }` and not read the state
 * in composition.
 */
fun Modifier.motionLayer(
    scale: () -> Float = { 1f },
    alpha: () -> Float = { 1f },
): Modifier =
    graphicsLayer {
        this.alpha = alpha()
        scaleX = scale()
        scaleY = scale()
    }
