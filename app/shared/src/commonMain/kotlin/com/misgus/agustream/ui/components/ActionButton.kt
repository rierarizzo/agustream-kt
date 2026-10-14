package com.misgus.agustream.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.misgus.agustream.ui.theme.AppAlpha
import com.misgus.agustream.ui.theme.AppCorner
import com.misgus.agustream.ui.theme.AppIconSize
import com.misgus.agustream.ui.theme.AppSizes
import com.misgus.agustream.ui.theme.AppSpacing

/**
 * Visual weight of an [ActionButton], from highest to lowest emphasis.
 *
 * A screen has one [Filled] or [Accent] call to action; everything else steps
 * down through [Tonal], [Outline] and [Text]. Keeping the weights in an enum
 * (instead of booleans) means a new variant is one branch here, not another
 * parameter on every call site.
 */
enum class ActionButtonStyle {
    /** High-contrast inverse surface: the default call to action. */
    Filled,

    /** Amber brand fill: the featured call to action (the billboards). */
    Accent,

    /** Translucent on-surface fill: a secondary action, readable over artwork. */
    Tonal,

    /** Transparent with an outline: a low-emphasis action. */
    Outline,

    /** Label only: the lowest emphasis, for an inline or tertiary action. */
    Text,
}

/**
 * The app's single call-to-action button, used for every action.
 *
 * The container and content colours come from the scheme per [style], never from
 * the call site. The content colour is applied explicitly to the icon and the
 * label, so it does not depend on the colour around it, and the focused control
 * draws a ring so keyboard and D-pad navigation stay visible.
 */
@Composable
fun ActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    style: ActionButtonStyle = ActionButtonStyle.Filled,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    val scheme = MaterialTheme.colorScheme
    // The brand colour has one source: the scheme's primary roles. The filled
    // call to action is the one high-contrast (light) surface on the dark UI.
    val container =
        when (style) {
            ActionButtonStyle.Filled -> scheme.inverseSurface
            ActionButtonStyle.Accent -> scheme.primary
            ActionButtonStyle.Tonal -> scheme.onSurface.copy(alpha = AppAlpha.HERO_ACTION)
            ActionButtonStyle.Outline, ActionButtonStyle.Text -> Color.Transparent
        }
    val content =
        when (style) {
            ActionButtonStyle.Filled -> scheme.inverseOnSurface
            ActionButtonStyle.Accent -> scheme.onPrimary
            ActionButtonStyle.Tonal, ActionButtonStyle.Outline, ActionButtonStyle.Text -> scheme.onSurface
        }
    // The amber ring disappears on the amber fill, so the featured button uses
    // the on-surface role for its focus ring instead.
    val focusRing = if (style == ActionButtonStyle.Accent) scheme.onSurface else scheme.primary
    val interaction = rememberInteraction()
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        shape = MaterialTheme.shapes.small,
        colors = ButtonDefaults.buttonColors(containerColor = container, contentColor = content),
        border =
            if (style == ActionButtonStyle.Outline) {
                BorderStroke(width = 1.dp, color = scheme.outline)
            } else {
                null
            },
        contentPadding = PaddingValues(horizontal = AppSpacing.lg, vertical = AppSpacing.sm),
        interactionSource = interaction.source,
        modifier =
            modifier
                .motionBorder(
                    color = { if (interaction.focused) focusRing else Color.Transparent },
                    width = AppSizes.focusRing,
                    cornerRadius = AppCorner.small,
                ).height(AppSizes.controlHeight),
    ) {
        when {
            loading ->
                CircularProgressIndicator(
                    modifier = Modifier.size(AppIconSize.sm),
                    strokeWidth = 2.dp,
                    color = content,
                )
            icon != null -> Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(AppIconSize.md))
        }
        if (loading || icon != null) Spacer(Modifier.width(AppSpacing.sm))
        Text(text, color = content)
    }
}
