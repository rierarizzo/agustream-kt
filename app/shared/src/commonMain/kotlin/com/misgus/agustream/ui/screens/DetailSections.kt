package com.misgus.agustream.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.misgus.agustream.core.model.MetaPerson
import com.misgus.agustream.core.model.MetaPreview
import com.misgus.agustream.core.model.MetaVideo
import com.misgus.agustream.ui.components.PosterImage
import com.misgus.agustream.ui.components.clickableSurface
import com.misgus.agustream.ui.components.rememberInteraction
import com.misgus.agustream.ui.theme.AppColors
import com.misgus.agustream.ui.theme.AppRadii
import com.misgus.agustream.ui.theme.AppSpacing
import com.misgus.agustream.ui.theme.PosterCaption

/** Cast or crew carousel of the detail screen. */
@Composable
internal fun PeopleRow(
    title: String,
    people: List<MetaPerson>,
) {
    Column {
        Text(
            title,
            modifier = Modifier.padding(horizontal = AppSpacing.xl),
            style = MaterialTheme.typography.titleLarge,
        )
        Spacer(Modifier.height(AppSpacing.md))
        LazyRow(
            modifier = Modifier.height(200.dp),
            contentPadding = PaddingValues(horizontal = AppSpacing.xl),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            items(people) { person -> PersonCard(person) }
        }
    }
}

@Composable
private fun PersonCard(person: MetaPerson) {
    Column(modifier = Modifier.width(128.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(88.dp).clip(CircleShape).background(AppColors.surfaceHigh),
            contentAlignment = Alignment.Center,
        ) {
            val photo = person.photo
            if (!photo.isNullOrEmpty()) {
                AsyncImage(
                    model = photo,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Text(
                    person.name.firstOrNull()?.uppercase() ?: "?",
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
        }
        Spacer(Modifier.height(AppSpacing.sm))
        Text(
            person.name,
            maxLines = 2,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
            style = PosterCaption.title(),
        )
        person.character?.takeIf { it.isNotEmpty() }?.let {
            Text(
                it,
                maxLines = 2,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis,
                style = PosterCaption.meta(),
            )
        }
    }
}

/** Label/value block with release data (released date, country, ...). */
@Composable
internal fun DetailsSection(entries: List<Pair<String, String>>) {
    Column {
        Row(
            modifier = Modifier.padding(horizontal = AppSpacing.xl),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xl * 2),
        ) {
            entries.forEach { (label, value) ->
                Column {
                    Text(
                        label.uppercase(),
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.textSecondary,
                    )
                    Spacer(Modifier.height(AppSpacing.xs))
                    Text(value, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

/** "You may also like" carousel. */
@Composable
internal fun SimilarRow(
    items: List<MetaPreview>,
    onOpen: (MetaPreview) -> Unit,
) {
    Column {
        Text(
            "You May Also Like",
            modifier = Modifier.padding(horizontal = AppSpacing.xl),
            style = MaterialTheme.typography.titleLarge,
        )
        Spacer(Modifier.height(AppSpacing.sm))
        LazyRow(
            modifier = Modifier.height(280.dp),
            contentPadding = PaddingValues(horizontal = AppSpacing.xl),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            items(items) { item ->
                val interaction = rememberInteraction()
                val borderColor by animateColorAsState(
                    targetValue = if (interaction.active) AppColors.accent else AppColors.divider,
                )
                Column(
                    modifier = Modifier.width(140.dp).clickableSurface(interaction, onClick = { onOpen(item) }),
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .aspectRatio(2f / 3f)
                            .border(1.dp, borderColor, RoundedCornerShape(AppRadii.md))
                            .clip(RoundedCornerShape(AppRadii.md)),
                    ) {
                        PosterImage(item.poster, Modifier.fillMaxSize())
                    }
                    Spacer(Modifier.height(AppSpacing.sm))
                    Text(
                        item.name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = PosterCaption.title(),
                    )
                    item.releaseInfo?.takeIf { it.isNotEmpty() }?.let {
                        Text(
                            it,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = PosterCaption.meta(),
                        )
                    }
                }
            }
        }
    }
}

/** Episode picker with a season selector for series. */
@Composable
internal fun EpisodesSection(
    videos: List<MetaVideo>,
    onPlayEpisode: (MetaVideo) -> Unit,
) {
    val seasons = remember(videos) { videos.mapNotNull { it.season }.distinct().sorted() }
    if (seasons.isEmpty()) return
    var season by remember { mutableStateOf(seasons.first()) }
    val episodes =
        remember(videos, season) {
            videos.filter { it.season == season }.sortedBy { it.episode ?: it.number ?: 0 }
        }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.xl),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Episodes", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            if (seasons.size > 1) {
                Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                    seasons.forEach { value ->
                        TextButton(onClick = { season = value }) {
                            Text(
                                "S$value",
                                color = if (value == season) AppColors.accent else AppColors.textSecondary,
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(AppSpacing.md))
        LazyRow(
            modifier = Modifier.height(190.dp),
            contentPadding = PaddingValues(horizontal = AppSpacing.xl),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            items(episodes) { video -> EpisodeCard(video) { onPlayEpisode(video) } }
        }
    }
}

@Composable
private fun EpisodeCard(
    video: MetaVideo,
    onTap: () -> Unit,
) {
    val number = video.episode ?: video.number
    val title = video.title ?: "Episode ${number ?: ""}".trim()
    val interaction = rememberInteraction()
    val borderColor by animateColorAsState(
        targetValue = if (interaction.active) AppColors.accent else AppColors.divider,
    )
    Column(modifier = Modifier.width(220.dp).clickableSurface(interaction, onTap)) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .border(1.dp, borderColor, RoundedCornerShape(AppRadii.md))
                .clip(RoundedCornerShape(AppRadii.md)),
        ) {
            PosterImage(video.thumbnail, Modifier.fillMaxSize())
        }
        Spacer(Modifier.height(AppSpacing.sm))
        Text(
            "${number?.let { "$it. " } ?: ""}$title".trim(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyLarge,
        )
        video.released?.takeIf { it.isNotEmpty() }?.let {
            Text(it, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

/** 1–10 rating dialog for the current title. */
@Composable
internal fun RatingDialog(
    current: Int?,
    onDismiss: () -> Unit,
    onSave: (Int?) -> Unit,
) {
    var value by remember { mutableStateOf(current ?: 0) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Your rating") },
        text = {
            Column {
                Text(
                    if (value == 0) "Not rated" else "$value / 10",
                    style = MaterialTheme.typography.titleLarge,
                )
                Slider(
                    value = value.toFloat(),
                    onValueChange = { value = it.toInt() },
                    valueRange = 0f..10f,
                    steps = 9,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(if (value == 0) null else value)
                onDismiss()
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = {
                    onSave(null)
                    onDismiss()
                }) { Text("Clear") }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        },
    )
}
