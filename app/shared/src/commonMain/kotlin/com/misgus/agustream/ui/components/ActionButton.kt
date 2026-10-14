package com.misgus.agustream.ui.components

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.misgus.agustream.ui.theme.AppAlpha
import com.misgus.agustream.ui.theme.AppIconSize
import com.misgus.agustream.ui.theme.AppSizes
import com.misgus.agustream.ui.theme.AppSpacing

/**
 * The app's single call-to-action button, used for every action.
 *
 * [primary] is the high-contrast inverse surface with inverse-on-surface
 * content; [accent] paints it in the amber brand colour (the scheme's primary
 * roles); the secondary variant is a translucent on-surface fill. The content
 * colour is applied explicitly to the icon and the label, so it never depends
 * on the surrounding content colour.
 */
@Composable
fun ActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    primary: Boolean = true,
    accent: Boolean = false,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    val scheme = MaterialTheme.colorScheme
    // The primary call to action is the one high-contrast (light) surface on the
    // dark UI, which is exactly the Material inverse roles. The accent variant
    // reads the amber from the scheme, so the brand colour has one source.
    val container =
        when {
            accent -> scheme.primary
            primary -> scheme.inverseSurface
            else -> scheme.onSurface.copy(alpha = AppAlpha.HERO_ACTION)
        }
    val content =
        when {
            accent -> scheme.onPrimary
            primary -> scheme.inverseOnSurface
            else -> scheme.onSurface
        }
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        shape = MaterialTheme.shapes.small,
        colors = ButtonDefaults.buttonColors(containerColor = container, contentColor = content),
        contentPadding = PaddingValues(horizontal = AppSpacing.lg, vertical = AppSpacing.sm),
        modifier = modifier.height(AppSizes.controlHeight),
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
