package com.misgus.agustream.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.misgus.agustream.ui.theme.AppAlpha

/**
 * Hover / focus / pressed state of a custom interactive surface.
 *
 * Compose has no shared "hovered" concept for plain [Modifier.clickable] targets,
 * so this is remembered once per element and reused by both the click handling
 * and the visual feedback.
 */
class InteractionState internal constructor(
    val source: MutableInteractionSource,
    val hovered: Boolean,
    val focused: Boolean,
    val pressed: Boolean,
) {
    /** True while the pointer is over the element or it holds keyboard focus. */
    val active: Boolean get() = hovered || focused
}

@Composable
fun rememberInteraction(): InteractionState {
    val source = remember { MutableInteractionSource() }
    val hovered by source.collectIsHoveredAsState()
    val focused by source.collectIsFocusedAsState()
    val pressed by source.collectIsPressedAsState()
    return InteractionState(source, hovered, focused, pressed)
}

/** Click handling for a custom surface, without the default ripple. */
@Composable
fun Modifier.clickableSurface(
    state: InteractionState,
    onClick: () -> Unit,
    enabled: Boolean = true,
): Modifier =
    clickable(
        interactionSource = state.source,
        indication = null,
        enabled = enabled,
        onClick = onClick,
    )

/** Background tint for [state], falling back to [base] when idle. */
fun interactionHighlight(
    state: InteractionState,
    base: Color = Color.Transparent,
): Color =
    when {
        state.pressed -> Color.White.copy(alpha = AppAlpha.INTERACT_PRESSED)
        state.focused -> Color.White.copy(alpha = AppAlpha.INTERACT_FOCUS)
        state.hovered -> Color.White.copy(alpha = AppAlpha.INTERACT_HOVER)
        else -> base
    }
