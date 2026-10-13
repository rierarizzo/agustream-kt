package com.misgus.agustream.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.misgus.agustream.ui.theme.AppCorner
import com.misgus.agustream.ui.theme.AppIconSize
import com.misgus.agustream.ui.theme.AppSizes
import com.misgus.agustream.ui.theme.AppSpacing

/**
 * The app's single call-to-action button, used for every action.
 *
 * [primary] is the solid white button with dark content; the secondary variant is
 * translucent. The content colour is applied explicitly to the icon and the
 * label, so it never depends on the surrounding content colour.
 */
@Composable
fun PillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    primary: Boolean = true,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    val scheme = MaterialTheme.colorScheme
    // The primary call to action is the one high-contrast (light) surface on the
    // dark UI, which is exactly the Material inverse roles.
    val container = if (primary) scheme.inverseSurface else scheme.onSurface.copy(alpha = AppAlpha.HERO_ACTION)
    val content = if (primary) scheme.inverseOnSurface else scheme.onSurface
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        shape = RoundedCornerShape(AppCorner.full),
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
