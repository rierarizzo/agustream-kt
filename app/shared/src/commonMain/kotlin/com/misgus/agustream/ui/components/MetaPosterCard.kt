package com.misgus.agustream.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import com.misgus.agustream.core.model.MetaPreview
import com.misgus.agustream.ui.theme.AppRadii
import com.misgus.agustream.ui.theme.AppSpacing
import com.misgus.agustream.ui.theme.PosterCaption

/**
 * Poster card for a catalog item ([MetaPreview]), used by Home rows.
 *
 * [width] is the fixed width for horizontal rows; when null the card fills its
 * parent (used inside a grid).
 */
@Composable
fun MetaPosterCard(
    preview: MetaPreview,
    watched: Boolean = false,
    width: Dp? = null,
    onTap: () -> Unit,
) {
    val shape = RoundedCornerShape(AppRadii.md)
    Column(
        modifier =
            Modifier
                .then(if (width != null) Modifier.width(width) else Modifier)
                .fillMaxHeight()
                .clip(shape)
                .clickable(onClick = onTap),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(shape),
        ) {
            PosterImage(preview.poster, Modifier.fillMaxWidth().fillMaxHeight())
            if (watched) {
                Box(Modifier.align(Alignment.TopEnd).padding(AppSpacing.xs)) { WatchedBadge() }
            }
        }
        Spacer(Modifier.height(AppSpacing.sm))
        Text(
            text = preview.name,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = PosterCaption.title(MaterialTheme.typography.bodyLarge),
        )
        val releaseInfo = preview.releaseInfo
        if (!releaseInfo.isNullOrEmpty()) {
            Text(
                text = releaseInfo,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = PosterCaption.year(MaterialTheme.typography.bodyMedium),
            )
        }
    }
}
