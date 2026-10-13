package com.misgus.agustream.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import com.misgus.agustream.core.model.BackendProfile

/**
 * Circular profile avatar, shared by the top bar and the profile picker.
 *
 * Shows the remote image when available, otherwise the initial of the name, and
 * a generic person icon when no profile is selected. [size] and [initialStyle]
 * let the caller scale it (small in the top bar, large in the picker).
 */
@Composable
fun ProfileAvatar(
    profile: BackendProfile?,
    size: Dp,
    initialStyle: TextStyle,
    modifier: Modifier = Modifier,
) {
    val color = profile?.avatarColorHex?.let(::parseHexColor) ?: MaterialTheme.colorScheme.surfaceContainerHigh
    Box(
        modifier = modifier.size(size).clip(CircleShape).background(color),
        contentAlignment = Alignment.Center,
    ) {
        val avatarUrl = profile?.avatarUrl
        when {
            !avatarUrl.isNullOrEmpty() && avatarUrl.startsWith("http") -> {
                Box(Modifier.size(size).clip(CircleShape)) {
                    PosterImage(avatarUrl, Modifier.size(size))
                }
            }
            profile != null -> {
                val initial =
                    profile.name
                        .trim()
                        .firstOrNull()
                        ?.uppercase() ?: "?"
                Text(text = initial, style = initialStyle, color = MaterialTheme.colorScheme.onSurface)
            }
            else ->
                Icon(
                    Icons.Outlined.Person,
                    contentDescription = "Profile",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(size * 0.56f),
                )
        }
    }
}

/** Parses `#RRGGBB` / `RRGGBB`; null when malformed. */
internal fun parseHexColor(value: String): Color? {
    val hex = value.removePrefix("#").trim()
    if (hex.length != 6) return null
    val parsed = hex.toLongOrNull(16) ?: return null
    return Color(0xFF000000L or parsed)
}
