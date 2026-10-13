package com.misgus.agustream.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.misgus.agustream.core.model.MetaPerson
import com.misgus.agustream.core.model.MetaPreview
import com.misgus.agustream.core.model.MetaVideo
import com.misgus.agustream.ui.components.MarqueeText
import com.misgus.agustream.ui.components.PosterImage
import com.misgus.agustream.ui.components.RemoteImage
import com.misgus.agustream.ui.components.clickableSurface
import com.misgus.agustream.ui.components.motionBorder
import com.misgus.agustream.ui.components.rememberInteraction
import com.misgus.agustream.ui.services.LocalAppServices
import com.misgus.agustream.ui.theme.AppCorner
import com.misgus.agustream.ui.theme.AppSpacing
import com.misgus.agustream.ui.theme.LocalWindowSize
import com.misgus.agustream.ui.theme.PosterCaption
import com.misgus.agustream.ui.theme.cellWidth
import com.misgus.agustream.ui.theme.gridSpacing
import com.misgus.agustream.ui.theme.motionTween
import com.misgus.agustream.ui.theme.railHeight
import com.misgus.agustream.ui.theme.screenPadding

/** Cast or crew carousel of the detail screen. */
@Composable
internal fun PeopleRow(
    title: String,
    people: List<MetaPerson>,
) {
    val windowSize = LocalWindowSize.current
    Column {
        Text(
            title,
            modifier = Modifier.padding(horizontal = windowSize.screenPadding),
            style = MaterialTheme.typography.titleLarge,
        )
        Spacer(Modifier.height(AppSpacing.md))
        LazyRow(
            modifier = Modifier.height(200.dp),
            contentPadding = PaddingValues(horizontal = windowSize.screenPadding),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            items(people) { person -> PersonCard(person) }
        }
    }
}

@Composable
private fun PersonCard(person: MetaPerson) {
    val interaction = rememberInteraction()
    Column(
        modifier = Modifier.width(128.dp).hoverable(interaction.source),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.size(88.dp).clip(MaterialTheme.shapes.small).background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center,
        ) {
            val photo = person.photo
            if (!photo.isNullOrEmpty()) {
                RemoteImage(
                    url = photo,
                    contentDescription = null,
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
        MarqueeText(
            text = person.name,
            style = PosterCaption.title(),
            active = interaction.active,
            textAlign = TextAlign.Center,
        )
        person.character?.takeIf { it.isNotEmpty() }?.let {
            MarqueeText(
                text = it,
                style = PosterCaption.meta(),
                active = interaction.active,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/** Label/value block with release data (released date, country, ...). */
@Composable
internal fun DetailsSection(entries: List<Pair<String, String>>) {
    val windowSize = LocalWindowSize.current
    Column {
        Row(
            modifier = Modifier.padding(horizontal = windowSize.screenPadding),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xl * 2),
        ) {
            entries.forEach { (label, value) ->
                Column {
                    Text(
                        label.uppercase(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
    // The same size as the catalog grids, so a title looks the same everywhere.
    val settingsRepository = LocalAppServices.current.settings
    val settings by settingsRepository.state.collectAsStateWithLifecycle()
    val posterSize = settings.posterSize
    val windowSize = LocalWindowSize.current
    Column {
        Text(
            "You May Also Like",
            modifier = Modifier.padding(horizontal = AppSpacing.xl),
            style = MaterialTheme.typography.titleLarge,
        )
        Spacer(Modifier.height(AppSpacing.sm))
        LazyRow(
            modifier = Modifier.height(posterSize.railHeight(windowSize)),
            contentPadding = PaddingValues(horizontal = windowSize.screenPadding),
            horizontalArrangement = Arrangement.spacedBy(windowSize.gridSpacing),
        ) {
            items(items) { item ->
                val interaction = rememberInteraction()
                val borderColor =
                    animateColorAsState(
                        targetValue =
                            if (interaction.active) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outlineVariant
                            },
                        animationSpec = motionTween(),
                        label = "borderColor",
                    )
                Column(
                    modifier = Modifier.width(posterSize.cellWidth(windowSize)).clickableSurface(interaction, onClick = { onOpen(item) }),
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .aspectRatio(2f / 3f)
                            .motionBorder(color = { borderColor.value }, width = 1.dp, cornerRadius = AppCorner.small)
                            .clip(MaterialTheme.shapes.small),
                    ) {
                        PosterImage(item.poster, Modifier.fillMaxSize())
                    }
                    Spacer(Modifier.height(AppSpacing.sm))
                    MarqueeText(
                        text = item.name,
                        style = PosterCaption.title(),
                        active = interaction.active,
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
    val seasons = remember(videos) { orderSeasons(videos.mapNotNull { it.season }.distinct()) }
    if (seasons.isEmpty()) return
    val windowSize = LocalWindowSize.current
    var season by remember(seasons) { mutableStateOf(defaultSeason(seasons)) }
    val episodes =
        remember(videos, season) {
            videos.filter { it.season == season }.sortedBy { it.episode ?: it.number ?: 0 }
        }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = windowSize.screenPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Episodes", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            if (seasons.size > 1) {
                Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                    seasons.forEach { value ->
                        TextButton(onClick = { season = value }) {
                            Text(
                                seasonLabel(value),
                                color =
                                    if (value == season) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(AppSpacing.md))
        LazyRow(
            modifier = Modifier.height(190.dp),
            contentPadding = PaddingValues(horizontal = windowSize.screenPadding),
            horizontalArrangement = Arrangement.spacedBy(windowSize.gridSpacing),
        ) {
            items(episodes) { video -> EpisodeCard(video) { onPlayEpisode(video) } }
        }
    }
}

/**
 * Seasons in display order: regular ones ascending first, then season `0`
 * ("Specials") at the end.
 */
private fun orderSeasons(seasons: List<Int>): List<Int> =
    seasons.filter { it != 0 }.sorted().let { regular ->
        if (seasons.contains(0)) regular + 0 else regular
    }

/** Season 1 opens the picker when present; otherwise the first season shown. */
private fun defaultSeason(seasons: List<Int>): Int = seasons.firstOrNull { it == 1 } ?: seasons.first()

/** Season `0` is presented as "Specials"; every other season as `S<n>`. */
private fun seasonLabel(season: Int): String = if (season == 0) "Specials" else "S$season"

private val RELEASE_MONTHS =
    listOf(
        "January",
        "February",
        "March",
        "April",
        "May",
        "June",
        "July",
        "August",
        "September",
        "October",
        "November",
        "December",
    )

private val RELEASE_DATE = Regex("(\\d{4})-(\\d{2})-(\\d{2})")

/**
 * Formats an addon release value (`2024-03-01` or a full ISO datetime) as a
 * short, readable English date such as `1 March 2024`.
 *
 * Returns `null` when there is no usable date, so the caller can omit the line.
 */
internal fun formatReleaseDate(raw: String?): String? {
    val match = RELEASE_DATE.find(raw ?: "") ?: return null
    val (year, month, day) = match.destructured
    val monthName = RELEASE_MONTHS.getOrNull(month.toInt() - 1) ?: return null
    return "${day.toInt()} $monthName $year"
}

@Composable
private fun EpisodeCard(
    video: MetaVideo,
    onTap: () -> Unit,
) {
    val number = video.episode ?: video.number
    val title = video.title ?: "Episode ${number ?: ""}".trim()
    val interaction = rememberInteraction()
    val borderColor =
        animateColorAsState(
            targetValue = if (interaction.active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
            animationSpec = motionTween(),
            label = "borderColor",
        )
    Column(modifier = Modifier.width(220.dp).clickableSurface(interaction, onTap)) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .motionBorder(color = { borderColor.value }, width = 1.dp, cornerRadius = AppCorner.small)
                .clip(MaterialTheme.shapes.small),
        ) {
            PosterImage(video.thumbnail, Modifier.fillMaxSize())
        }
        Spacer(Modifier.height(AppSpacing.sm))
        MarqueeText(
            text = "${number?.let { "$it. " } ?: ""}$title".trim(),
            style = PosterCaption.title(),
            active = interaction.active,
        )
        formatReleaseDate(video.released)?.let {
            Text(it, maxLines = 1, overflow = TextOverflow.Ellipsis, style = PosterCaption.meta())
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
