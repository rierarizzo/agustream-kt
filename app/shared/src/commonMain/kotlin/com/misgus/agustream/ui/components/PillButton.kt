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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.misgus.agustream.ui.theme.AppAlpha
import com.misgus.agustream.ui.theme.AppIconSize
import com.misgus.agustream.ui.theme.AppRadii
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
    val container = if (primary) Color.White else Color.White.copy(alpha = AppAlpha.HERO_ACTION)
    val content = if (primary) Color.Black else Color.White
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        shape = RoundedCornerShape(AppRadii.md),
        colors = ButtonDefaults.buttonColors(containerColor = container, contentColor = content),
        contentPadding = PaddingValues(horizontal = AppSpacing.lg, vertical = AppSpacing.sm),
        modifier = modifier.height(44.dp),
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
