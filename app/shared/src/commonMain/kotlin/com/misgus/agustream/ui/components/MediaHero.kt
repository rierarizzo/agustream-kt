package com.misgus.agustream.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.misgus.agustream.ui.theme.AppColors
import com.misgus.agustream.ui.theme.AppSpacing

/** Title logo when the metadata provides one, otherwise the name as a headline. */
@Composable
fun MediaTitle(
    name: String,
    logo: String?,
) {
    if (!logo.isNullOrEmpty()) {
        AsyncImage(
            model = logo,
            contentDescription = name,
            modifier = Modifier.heightIn(max = 140.dp).widthIn(max = 480.dp),
        )
    } else {
        Text(
            name,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.displayMedium,
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
                    color = AppColors.textSecondary,
                    modifier = Modifier.padding(horizontal = AppSpacing.sm),
                )
            }
            Text(part, style = MaterialTheme.typography.labelLarge, color = AppColors.textPrimary)
        }
    }
}

/** Formats a rating to one decimal place. */
fun ratingText(value: Double): String = (kotlin.math.round(value * 10) / 10.0).toString()
