package com.misgus.agustream.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.misgus.agustream.ui.theme.AppColors
import com.misgus.agustream.ui.theme.AppIconSize
import com.misgus.agustream.ui.theme.AppSizes

/**
 * Icon-only action, used for controls that sit over artwork (back, refresh,
 * close, calendar paging).
 *
 * Material's `IconButton` has no scrim and no focus ring, so it disappears over
 * a busy header. This one keeps the app's 48dp target, draws a state layer and a
 * keyboard focus ring, and can sit on a media [scrim] so it stays legible over
 * any image.
 */
@Composable
fun AppIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    scrim: Boolean = false,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    iconModifier: Modifier = Modifier,
) {
    val interaction = rememberInteraction()
    Box(
        modifier =
            modifier
                .focusRing(interaction)
                .size(AppSizes.minTouchTarget)
                .clip(MaterialTheme.shapes.small)
                .background(if (scrim) AppColors.mediaScrim else Color.Transparent)
                .clickableSurface(interaction, onClick, enabled = enabled),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = iconModifier.size(AppIconSize.lg),
        )
    }
}
