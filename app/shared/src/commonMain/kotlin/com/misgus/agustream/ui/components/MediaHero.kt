package com.misgus.agustream.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.misgus.agustream.ui.theme.AppSpacing
import com.misgus.agustream.ui.theme.AppType
import com.misgus.agustream.ui.theme.LocalWindowSize
import com.misgus.agustream.ui.theme.WindowSize

/** Title logo when the metadata provides one, otherwise the name as a headline. */
@Composable
fun MediaTitle(
    name: String,
    logo: String?,
    /** While the metadata loads, reserve the logo's space without drawing anything. */
    loading: Boolean = false,
) {
    // The logo is a fixed-height asset; it is smaller on a phone so the hero
    // does not turn into a wall of artwork.
    val logoHeight =
        if (LocalWindowSize.current == WindowSize.COMPACT) COMPACT_LOGO_HEIGHT else LOGO_HEIGHT
    when {
        loading ->
            // Reserve the space so the title does not jump when it arrives, but
            // draw nothing: a skeleton here reads as a stray block.
            Spacer(Modifier.height(logoHeight))
        !logo.isNullOrEmpty() ->
            // A fixed height keeps every logo the same visual weight; the width
            // follows the asset's aspect ratio and the hero column bounds it.
            RemoteImage(
                url = logo,
                contentDescription = name,
                contentScale = ContentScale.Fit,
                filterQuality = LogoFilterQuality,
                fadeIn = true,
                modifier = Modifier.height(logoHeight),
            )
        else ->
            Text(
                name,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = AppType.heroTitle(),
            )
    }
}

/** Dot-separated metadata line, e.g. `2023 • 1 h 37 min • ★ 7.8`. */
@Composable
fun MetadataRow(parts: List<String>) {
    if (parts.isEmpty()) return
    Spacer(Modifier.height(AppSpacing.md))
    Row(verticalAlignment = Alignment.CenterVertically) {
        parts.forEachIndexed { index, part ->
            if (index > 0) {
                Text(
                    "•",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = AppSpacing.sm),
                )
            }
            Text(part, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

/** Formats a rating to one decimal place. */
fun ratingText(value: Double): String = (kotlin.math.round(value * 10) / 10.0).toString()

/** Height every title logo is painted at on a wide window. */
private val LOGO_HEIGHT = 96.dp

/** Height of the title logo on a phone. */
private val COMPACT_LOGO_HEIGHT = 56.dp
