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
import androidx.compose.ui.unit.Dp
import com.misgus.agustream.ui.theme.AppLogoSize
import com.misgus.agustream.ui.theme.AppSpacing
import com.misgus.agustream.ui.theme.AppSynopsis
import com.misgus.agustream.ui.theme.AppType
import com.misgus.agustream.ui.theme.LocalWindowSize
import com.misgus.agustream.ui.theme.WindowSize

/*
 * Shared copy pieces for a media billboard.
 *
 * Both the home hero and the detail hero render their title, metadata, synopsis
 * and genres through these composables, so the two screens cannot drift in type,
 * colour, separators or ordering. Anything a billboard shows about a title
 * belongs here, not in the screen.
 */

/** Title logo when the metadata provides one, otherwise the name as a headline. */
@Composable
fun MediaTitle(
    name: String,
    logo: String?,
    /** Optical height of the logo; `null` uses the detail billboard's height. */
    logoHeight: Dp? = null,
    /** While the metadata loads, reserve the logo's space without drawing anything. */
    loading: Boolean = false,
) {
    // A fixed optical height keeps the logos in scale, capped by a max width so a
    // wide one cannot overflow the column. It is smaller on a phone.
    val height =
        logoHeight
            ?: if (LocalWindowSize.current == WindowSize.COMPACT) {
                AppLogoSize.compactHeight
            } else {
                AppLogoSize.detailHeight
            }
    when {
        loading ->
            // Reserve the space so the title does not jump when it arrives, but
            // draw nothing: a skeleton here reads as a stray block.
            Spacer(Modifier.height(height))
        !logo.isNullOrEmpty() ->
            RemoteImage(
                url = logo,
                contentDescription = name,
                contentScale = ContentScale.Fit,
                filterQuality = LogoFilterQuality,
                fadeIn = true,
                modifier = Modifier.height(height).widthIn(max = AppLogoSize.maxWidth),
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

/**
 * Dot-separated metadata line, e.g. `2023 • 1 h 37 min • ★ 7.8`.
 *
 * The caller owns the leading space, so each billboard keeps its own rhythm.
 */
@Composable
fun MetadataRow(parts: List<String>) {
    if (parts.isEmpty()) return
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

/**
 * Release info, genres, runtime and rating in the order a billboard shows them.
 *
 * [runtime] and [genres] are optional because a catalog preview may not carry
 * them; when absent the remaining parts keep their relative order. The detail
 * billboard shows genres under the synopsis instead, so it does not pass them.
 */
fun metadataParts(
    releaseInfo: String?,
    runtime: String? = null,
    genres: List<String> = emptyList(),
    rating: Double? = null,
): List<String> =
    buildList {
        releaseInfo?.takeIf { it.isNotEmpty() }?.let { add(it) }
        if (genres.isNotEmpty()) add(genres.joinToString(", "))
        runtime?.takeIf { it.isNotEmpty() }?.let { add(it) }
        rating?.let { add("★ ${ratingText(it)}") }
    }

/**
 * Hero synopsis: a teaser of at most [AppSynopsis.MAX_LINES] lines.
 *
 * [loading] keeps [minLines] reserved without showing the preview copy, so the
 * layout does not jump when the real synopsis arrives.
 */
@Composable
fun HeroSynopsis(
    text: String?,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    minLines: Int = 1,
) {
    Text(
        text = if (loading) "" else text.orEmpty(),
        minLines = minLines,
        maxLines = AppSynopsis.MAX_LINES,
        overflow = TextOverflow.Ellipsis,
        style = AppType.heroBody(),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.92f),
        modifier = modifier,
    )
}

/** Genres under the synopsis, dot-separated and dimmed. */
@Composable
fun HeroGenres(genres: List<String>) {
    if (genres.isEmpty()) return
    Text(
        genres.joinToString("  ·  "),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

/** Formats a rating to one decimal place. */
fun ratingText(value: Double): String = (kotlin.math.round(value * 10) / 10.0).toString()
