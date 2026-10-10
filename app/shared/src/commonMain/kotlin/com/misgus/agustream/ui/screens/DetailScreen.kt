package com.misgus.agustream.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.misgus.agustream.core.model.LibraryItem
import com.misgus.agustream.core.model.MetaPerson
import com.misgus.agustream.core.model.MetaPreview
import com.misgus.agustream.core.model.MetaVideo
import com.misgus.agustream.ui.components.Message
import com.misgus.agustream.ui.components.PosterImage
import com.misgus.agustream.ui.detail.DetailUiState
import com.misgus.agustream.ui.detail.DetailViewModel
import com.misgus.agustream.ui.services.LocalAppServices
import com.misgus.agustream.ui.streams.StreamsRequest
import com.misgus.agustream.ui.theme.AppColors
import com.misgus.agustream.ui.theme.AppRadii
import com.misgus.agustream.ui.theme.AppSpacing
import com.misgus.agustream.ui.theme.PosterCaption
import kotlinx.coroutines.launch

/** Title detail: hero, cast, crew, details and "More like this". */
@Composable
fun DetailScreen(
    item: LibraryItem,
    onBack: () -> Unit,
    onOpenDetail: (LibraryItem) -> Unit,
    onOpenStreams: (StreamsRequest) -> Unit,
) {
    val services = LocalAppServices.current
    val viewModel: DetailViewModel = viewModel(key = "detail-${item.contentId}") {
        DetailViewModel(services.metadata, item)
    }
    LaunchedEffect(item.contentId) { viewModel.load() }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    var saved by remember { mutableStateOf(false) }
    var savedKnown by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var watched by remember { mutableStateOf(false) }
    var watchedKnown by remember { mutableStateOf(false) }
    var rating by remember { mutableStateOf<Int?>(null) }
    var showRatingDialog by remember { mutableStateOf(false) }

    LaunchedEffect(item.contentId) {
        saved = runCatching { services.library.contains(item.contentId) }.getOrDefault(false)
        savedKnown = true
        watched = runCatching {
            services.progress.watchedEntries(listOf(item.contentId)).any { it.isMarker }
        }.getOrDefault(false)
        watchedKnown = true
        rating = runCatching { services.ratings.ratingOf(item.contentId) }.getOrNull()
    }

    val openStreams: (String, String, String) -> Unit = { type, id, title ->
        onOpenStreams(
            StreamsRequest(
                type = type,
                id = id,
                title = title,
                year = state.year?.toString(),
                background = state.background,
            ),
        )
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Hero(
            state = state,
            onBack = onBack,
            onPlay = {
                val first = state.episodes.firstOrNull()
                if (state.isSeries && first != null) {
                    openStreams("series", first.id, "${state.name} · ${first.title ?: "Episode"}")
                } else {
                    openStreams(item.contentType, item.contentId, state.name)
                }
            },
            saved = saved,
            savedBusy = !savedKnown || saving,
            onToggleSaved = {
                if (saving) return@Hero
                saving = true
                scope.launch {
                    runCatching {
                        if (saved) services.library.remove(item.contentId)
                        else services.library.add(item)
                    }.onSuccess { saved = !saved }
                    saving = false
                }
            },
            watched = watched,
            watchedBusy = !watchedKnown,
            onToggleWatched = {
                if (!watchedKnown) return@Hero
                watchedKnown = false
                scope.launch {
                    runCatching {
                        if (watched) services.progress.unmarkWatched(item.contentId)
                        else services.progress.markWatched(item.contentId, item.contentType)
                    }.onSuccess { watched = !watched }
                    watchedKnown = true
                }
            },
            rating = rating,
            onRate = { showRatingDialog = true },
        )
        Spacer(Modifier.height(AppSpacing.lg))
        if (state.isSeries && state.episodes.isNotEmpty()) {
            EpisodesSection(state.episodes) { video ->
                openStreams("series", video.id, "${state.name} · ${video.title ?: "Episode"}")
            }
        }
        if (state.cast.isNotEmpty()) {
            Spacer(Modifier.height(AppSpacing.lg))
            PeopleRow("Cast", state.cast)
        }
        if (state.crew.isNotEmpty()) {
            Spacer(Modifier.height(AppSpacing.lg))
            PeopleRow("Crew", state.crew)
        }
        val details = buildList {
            state.released?.takeIf { it.isNotEmpty() }?.let { add("Released" to it) }
            state.country?.takeIf { it.isNotEmpty() }?.let { add("Country" to it) }
        }
        if (details.isNotEmpty()) {
            Spacer(Modifier.height(AppSpacing.lg))
            DetailsSection(details)
        }
        if (state.similar.isNotEmpty()) {
            Spacer(Modifier.height(AppSpacing.lg))
            SimilarRow(state.similar) { preview ->
                onOpenDetail(
                    LibraryItem(
                        id = preview.id,
                        contentId = preview.id,
                        contentType = preview.type.ifEmpty { item.contentType },
                        name = preview.name,
                        poster = preview.poster,
                        background = preview.background,
                        logo = preview.logo,
                        description = preview.description,
                        releaseInfo = preview.releaseInfo,
                        imdbRating = preview.imdbRating,
                        genres = preview.genres,
                        addonBaseUrl = item.addonBaseUrl,
                    ),
                )
            }
        }
        Spacer(Modifier.height(AppSpacing.xl))
    }

    if (showRatingDialog) {
        RatingDialog(current = rating, onDismiss = { showRatingDialog = false }) { value ->
            rating = value
            scope.launch { services.ratings.setRating(item.contentId, value) }
        }
    }
}

@Composable
private fun Hero(
    state: DetailUiState,
    onBack: () -> Unit,
    onPlay: () -> Unit,
    saved: Boolean,
    savedBusy: Boolean,
    onToggleSaved: () -> Unit,
    watched: Boolean,
    watchedBusy: Boolean,
    onToggleWatched: () -> Unit,
    rating: Int?,
    onRate: () -> Unit,
) {
    val backdrop = state.background ?: state.poster
    Box(Modifier.fillMaxWidth().height(480.dp)) {
        if (!backdrop.isNullOrEmpty()) {
            AsyncImage(
                model = backdrop,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Box(Modifier.fillMaxSize().background(AppColors.surface))
        }
        Box(
            Modifier.fillMaxSize().background(
                Brush.horizontalGradient(
                    0f to Color.Black.copy(alpha = 0.9f),
                    0.55f to Color.Black.copy(alpha = 0.35f),
                    1f to Color.Transparent,
                ),
            ),
        )
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    0f to Color.Black.copy(alpha = 0.25f),
                    0.5f to Color.Transparent,
                    1f to AppColors.background,
                ),
            ),
        )
        Row(
            modifier = Modifier.align(Alignment.BottomStart).padding(AppSpacing.xl),
            verticalAlignment = Alignment.Bottom,
        ) {
            Box(Modifier.width(224.dp).aspectRatio(2f / 3f).clip(RoundedCornerShape(AppRadii.md))) {
                PosterImage(state.poster, Modifier.fillMaxSize())
            }
            Spacer(Modifier.width(AppSpacing.lg))
            Info(
                modifier = Modifier.weight(1f),
                state = state,
                onPlay = onPlay,
                saved = saved,
                savedBusy = savedBusy,
                onToggleSaved = onToggleSaved,
                watched = watched,
                watchedBusy = watchedBusy,
                onToggleWatched = onToggleWatched,
                rating = rating,
                onRate = onRate,
            )
        }
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.TopStart).padding(AppSpacing.xs),
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Info(
    modifier: Modifier = Modifier,
    state: DetailUiState,
    onPlay: () -> Unit,
    saved: Boolean,
    savedBusy: Boolean,
    onToggleSaved: () -> Unit,
    watched: Boolean,
    watchedBusy: Boolean,
    onToggleWatched: () -> Unit,
    rating: Int?,
    onRate: () -> Unit,
) {
    Column(modifier) {
        val logo = state.logo
        if (!logo.isNullOrEmpty()) {
            AsyncImage(model = logo, contentDescription = null, modifier = Modifier.height(56.dp))
        } else {
            Text(
                state.name,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.displaySmall,
            )
        }
        val parts = buildList {
            state.releaseInfo?.takeIf { it.isNotEmpty() }?.let { add(it) }
            state.runtime?.takeIf { it.isNotEmpty() }?.let { add(it) }
            state.imdbRating?.let { add("★ ${round1(it)}") }
        }
        if (parts.isNotEmpty()) {
            Spacer(Modifier.height(AppSpacing.sm))
            Text(
                parts.joinToString("   •   "),
                style = MaterialTheme.typography.bodyLarge,
                color = AppColors.textSecondary,
            )
        }
        if (state.genres.isNotEmpty()) {
            Spacer(Modifier.height(AppSpacing.sm))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                state.genres.forEach { genre ->
                    Text(
                        genre,
                        modifier = Modifier
                            .clip(RoundedCornerShape(AppRadii.sm))
                            .background(AppColors.surfaceHigh)
                            .padding(horizontal = AppSpacing.sm + 2.dp, vertical = AppSpacing.xs),
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.textPrimary,
                    )
                }
            }
        }
        state.description?.takeIf { it.isNotEmpty() }?.let {
            Spacer(Modifier.height(AppSpacing.md))
            Text(
                it,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.width(720.dp),
            )
        }
        Spacer(Modifier.height(AppSpacing.md))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            TextButton(onClick = onPlay) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(AppSpacing.xs))
                Text("Play")
            }
            OutlinedButton(onClick = {}, enabled = false) {
                Icon(Icons.Outlined.Movie, contentDescription = null)
                Spacer(Modifier.width(AppSpacing.xs))
                Text("Trailer")
            }
            IconButton(enabled = !watchedBusy, onClick = onToggleWatched) {
                Icon(
                    if (watched) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                    contentDescription = if (watched) "Mark as unwatched" else "Mark as watched",
                    tint = if (watched) AppColors.accent else MaterialTheme.colorScheme.onSurface,
                )
            }
            IconButton(enabled = !savedBusy, onClick = onToggleSaved) {
                Icon(
                    if (saved) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = if (saved) "Remove from library" else "Add to library",
                    tint = if (saved) AppColors.accent else MaterialTheme.colorScheme.onSurface,
                )
            }
            IconButton(onClick = onRate) {
                Icon(
                    if (rating != null) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = "Rate",
                    tint = if (rating != null) AppColors.accent else MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun PeopleRow(title: String, people: List<MetaPerson>) {
    Column {
        Text(
            title,
            modifier = Modifier.padding(horizontal = AppSpacing.xl),
            style = MaterialTheme.typography.titleLarge,
        )
        Spacer(Modifier.height(AppSpacing.md))
        LazyRow(
            modifier = Modifier.height(176.dp),
            contentPadding = PaddingValues(horizontal = AppSpacing.xl),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            items(people) { person -> PersonCard(person) }
        }
    }
}

@Composable
private fun PersonCard(person: MetaPerson) {
    Column(modifier = Modifier.width(104.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(64.dp).clip(CircleShape).background(AppColors.surfaceHigh),
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
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        }
        Spacer(Modifier.height(AppSpacing.sm))
        Text(
            person.name,
            maxLines = 2,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyLarge,
        )
        person.character?.takeIf { it.isNotEmpty() }?.let {
            Text(
                it,
                maxLines = 2,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun DetailsSection(entries: List<Pair<String, String>>) {
    Column {
        Text(
            "Details",
            modifier = Modifier.padding(horizontal = AppSpacing.xl),
            style = MaterialTheme.typography.titleLarge,
        )
        Spacer(Modifier.height(AppSpacing.md))
        Row(
            modifier = Modifier.padding(horizontal = AppSpacing.xl),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xl * 2),
        ) {
            entries.forEach { (label, value) ->
                Column {
                    Text(
                        label.uppercase(),
                        style = MaterialTheme.typography.bodyMedium.copy(letterSpacing = 0.8.sp),
                    )
                    Spacer(Modifier.height(AppSpacing.xs))
                    Text(value, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
private fun SimilarRow(items: List<MetaPreview>, onOpen: (MetaPreview) -> Unit) {
    Column {
        Text(
            "More like this",
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
                Column(
                    modifier = Modifier.width(140.dp).clickable { onOpen(item) },
                ) {
                    Box(Modifier.fillMaxWidth().aspectRatio(2f / 3f).clip(RoundedCornerShape(AppRadii.md))) {
                        PosterImage(item.poster, Modifier.fillMaxSize())
                    }
                    Spacer(Modifier.height(AppSpacing.sm))
                    Text(
                        item.name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = PosterCaption.title(MaterialTheme.typography.bodyLarge),
                    )
                    item.releaseInfo?.takeIf { it.isNotEmpty() }?.let {
                        Text(
                            it,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = PosterCaption.year(MaterialTheme.typography.bodyMedium),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EpisodesSection(videos: List<MetaVideo>, onPlayEpisode: (MetaVideo) -> Unit) {
    val seasons = remember(videos) { videos.mapNotNull { it.season }.distinct().sorted() }
    if (seasons.isEmpty()) return
    var season by remember { mutableStateOf(seasons.first()) }
    val episodes = remember(videos, season) {
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
private fun EpisodeCard(video: MetaVideo, onTap: () -> Unit) {
    val number = video.episode ?: video.number
    val title = video.title ?: "Episode ${number ?: ""}".trim()
    Column(modifier = Modifier.width(220.dp).clickable(onClick = onTap)) {
        Box(Modifier.fillMaxWidth().aspectRatio(16f / 9f).clip(RoundedCornerShape(AppRadii.md))) {
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

@Composable
private fun RatingDialog(current: Int?, onDismiss: () -> Unit, onSave: (Int?) -> Unit) {
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
            TextButton(onClick = { onSave(if (value == 0) null else value); onDismiss() }) {
                Text("Save")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = { onSave(null); onDismiss() }) { Text("Clear") }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        },
    )
}

private fun round1(value: Double): String = (kotlin.math.round(value * 10) / 10.0).toString()
