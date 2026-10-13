package com.misgus.agustream.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import com.misgus.agustream.ui.theme.AppLogoSize
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
    // The logo keeps a fixed height for a consistent optical weight, capped by a
    // max width so a wide one cannot overflow the column. It is smaller on a
    // phone so the hero does not turn into a wall of artwork.
    val logoHeight =
        if (LocalWindowSize.current == WindowSize.COMPACT) AppLogoSize.compactHeight else AppLogoSize.detailHeight
    when {
        loading ->
            // Reserve the space so the title does not jump when it arrives, but
            // draw nothing: a skeleton here reads as a stray block.
            Spacer(Modifier.height(logoHeight))
        !logo.isNullOrEmpty() ->
            // A fixed height plus a width cap contains the logo without
            // cropping, so square and wide logos both stay in scale.
            RemoteImage(
                url = logo,
                contentDescription = name,
                contentScale = ContentScale.Fit,
                filterQuality = LogoFilterQuality,
                fadeIn = true,
                modifier = Modifier.height(logoHeight).widthIn(max = AppLogoSize.maxWidth),
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
