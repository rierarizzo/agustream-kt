package com.misgus.agustream.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import com.misgus.agustream.ui.theme.AppAlpha
import com.misgus.agustream.ui.theme.AppColors
import com.misgus.agustream.ui.theme.AppCorner
import com.misgus.agustream.ui.theme.AppIconSize
import com.misgus.agustream.ui.theme.AppMotion
import com.misgus.agustream.ui.theme.AppScale
import com.misgus.agustream.ui.theme.AppSizes
import com.misgus.agustream.ui.theme.motionLayer
import com.misgus.agustream.ui.theme.motionTween

/**
 * Icon-only action: a header control (back, refresh, close, calendar paging), a
 * player control, or a toggle such as the detail screen's save/rate/watched.
 *
 * Material's `IconButton` has no scrim and no focus ring, so it disappears over
 * a busy header and a keyboard user cannot see where focus is. This one keeps
 * the app's 48dp target, draws the shared state layer and focus ring, dims when
 * disabled or loading, and can sit on a media [scrim] so it stays legible over
 * any image. [selected] gives it the amber brand tint for a toggle, so one
 * component covers every icon case instead of each screen rolling its own.
 */
@Composable
fun AppIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selected: Boolean = false,
    loading: Boolean = false,
    scrim: Boolean = false,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    iconSize: Dp = AppIconSize.md,
    size: Dp = AppSizes.minTouchTarget,
    shape: Shape = MaterialTheme.shapes.small,
    focusRingCorner: Dp = AppCorner.small,
    iconModifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val interaction = rememberInteraction()
    val contentTint =
        when {
            !enabled -> tint.copy(alpha = AppAlpha.DISABLED_CONTENT)
            selected -> scheme.primary
            else -> tint
        }
    // While the real state is unknown, dim the glyph instead of showing it as
    // "off": it is not that it is not saved, it is that we do not know yet.
    val loadingAlpha by animateFloatAsState(
        targetValue = if (loading) AppAlpha.ACTION_LOADING else 1f,
        animationSpec = motionTween(AppMotion.FAST),
        label = "iconButtonAlpha",
    )
    val pressScale by animateFloatAsState(
        targetValue = if (interaction.pressed) AppScale.PRESS else 1f,
        animationSpec = motionTween(AppMotion.FAST),
        label = "iconButtonPressScale",
    )
    val baseBackground =
        when {
            scrim -> AppColors.mediaScrim
            selected -> scheme.primary.copy(alpha = AppAlpha.NAV_SELECTED)
            else -> Color.Transparent
        }
    Box(
        modifier =
            modifier
                .focusRing(interaction, cornerRadius = focusRingCorner)
                .size(size)
                .motionLayer(scale = { pressScale }, alpha = { loadingAlpha })
                .clip(shape)
                .background(interactionHighlight(interaction, base = baseBackground))
                .clickableSurface(interaction, onClick, enabled = enabled && !loading),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = contentTint,
            modifier = iconModifier.size(iconSize),
        )
    }
}
