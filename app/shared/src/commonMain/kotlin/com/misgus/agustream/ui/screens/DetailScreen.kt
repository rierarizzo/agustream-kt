package com.misgus.agustream.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.misgus.agustream.core.model.LibraryItem
import com.misgus.agustream.core.util.runCatchingCancellable
import com.misgus.agustream.ui.components.MediaTitle
import com.misgus.agustream.ui.components.MetadataRow
import com.misgus.agustream.ui.components.PillButton
import com.misgus.agustream.ui.components.clickableSurface
import com.misgus.agustream.ui.components.interactionHighlight
import com.misgus.agustream.ui.components.ratingText
import com.misgus.agustream.ui.components.rememberInteraction
import com.misgus.agustream.ui.detail.DetailUiState
import com.misgus.agustream.ui.detail.DetailViewModel
import com.misgus.agustream.ui.services.LocalAppServices
import com.misgus.agustream.ui.shell.LocalSnackbarHost
import com.misgus.agustream.ui.streams.StreamsRequest
import com.misgus.agustream.ui.theme.AppColors
import com.misgus.agustream.ui.theme.AppIconSize
import com.misgus.agustream.ui.theme.AppRadii
import com.misgus.agustream.ui.theme.AppScrim
import com.misgus.agustream.ui.theme.AppSpacing
import kotlinx.coroutines.launch

/** Title detail: an immersive hero, episodes and the extra sections. */
@Composable
fun DetailScreen(
    item: LibraryItem,
    onBack: () -> Unit,
    onOpenDetail: (LibraryItem) -> Unit,
    onOpenStreams: (StreamsRequest) -> Unit,
) {
    val services = LocalAppServices.current
    val viewModel: DetailViewModel =
        viewModel(key = "detail-${item.contentId}") {
            DetailViewModel(services.metadata, item)
        }
    LaunchedEffect(item.contentId) { viewModel.load() }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val snackbar = LocalSnackbarHost.current

    var saved by remember { mutableStateOf(false) }
    var savedKnown by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var watched by remember { mutableStateOf(false) }
    var watchedKnown by remember { mutableStateOf(false) }
    var rating by remember { mutableStateOf<Int?>(null) }
    var showRatingDialog by remember { mutableStateOf(false) }

    LaunchedEffect(item.contentId) {
        saved = runCatchingCancellable { services.library.contains(item.contentId) }.getOrDefault(false)
        savedKnown = true
        watched =
            runCatchingCancellable {
                services.progress.watchedEntries(listOf(item.contentId)).any { it.isMarker }
            }.getOrDefault(false)
        watchedKnown = true
        rating = runCatchingCancellable { services.ratings.ratingOf(item.contentId) }.getOrNull()
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

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val viewportHeight = maxHeight
        val scrollState = rememberScrollState()
        // The artwork is a fixed layer, so its darkening spans the whole page:
        // bright at the top, near-black at the end, never fully black.
        val fade by remember {
            derivedStateOf {
                val max = scrollState.maxValue
                if (max <= 0) 0f else (scrollState.value.toFloat() / max).coerceIn(0f, 1f)
            }
        }

        Box(Modifier.fillMaxSize()) {
            // Fixed below the scrolling content: the artwork follows the page
            // instead of leaving with the hero.
            DetailBackdrop(state = state, fade = fade)

            Column(Modifier.fillMaxSize().verticalScroll(scrollState)) {
                HeroContent(
                    state = state,
                    height = viewportHeight,
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
                        if (saving) return@HeroContent
                        saving = true
                        scope.launch {
                            runCatchingCancellable {
                                if (saved) {
                                    services.library.remove(item.contentId)
                                } else {
                                    services.library.add(item)
                                }
                            }.onSuccess { saved = !saved }
                                .onFailure { snackbar?.showSnackbar("Could not update your library.") }
                            saving = false
                        }
                    },
                    watched = watched,
                    watchedBusy = !watchedKnown,
                    onToggleWatched = {
                        if (!watchedKnown) return@HeroContent
                        watchedKnown = false
                        scope.launch {
                            runCatchingCancellable {
                                if (watched) {
                                    services.progress.unmarkWatched(item.contentId)
                                } else {
                                    services.progress.markWatched(item.contentId, item.contentType)
                                }
                            }.onSuccess { watched = !watched }
                                .onFailure { snackbar?.showSnackbar("Could not update the watched state.") }
                            watchedKnown = true
                        }
                    },
                    rating = rating,
                    onRate = { showRatingDialog = true },
                )

                DetailScrollContent(
                    state = state,
                    item = item,
                    openStreams = openStreams,
                    onOpenDetail = onOpenDetail,
                )
            }

            // Fixed top scrim: guarantees a dark strip behind the back button,
            // even when section titles scroll underneath it.
            Box(
                Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(TOP_SCRIM_HEIGHT)
                    .background(AppScrim.detailTop),
            )

            // Fixed, so it stays reachable while the page scrolls. It uses the
            // same plain icon button as the player and the catalog; the fixed
            // top scrim is what keeps it legible.
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.TopStart).padding(AppSpacing.md),
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
        }
    }

    if (showRatingDialog) {
        RatingDialog(current = rating, onDismiss = { showRatingDialog = false }) { value ->
            rating = value
            scope.launch {
                runCatchingCancellable { services.ratings.setRating(item.contentId, value) }
                    .onFailure { snackbar?.showSnackbar("Could not save your rating.") }
            }
        }
    }
}

@Composable
private fun DetailScrollContent(
    state: DetailUiState,
    item: LibraryItem,
    openStreams: (String, String, String) -> Unit,
    onOpenDetail: (LibraryItem) -> Unit,
) {
    if (state.isSeries && state.episodes.isNotEmpty()) {
        Spacer(Modifier.height(AppSpacing.lg))
        EpisodesSection(state.episodes) { video ->
            openStreams("series", video.id, "${state.name} · ${video.title ?: "Episode"}")
        }
    }

    val detailEntries =
        buildList {
            state.released?.takeIf { it.isNotEmpty() }?.let { add("Released" to it) }
            state.country?.takeIf { it.isNotEmpty() }?.let { add("Country" to it) }
        }
    if (state.cast.isNotEmpty()) {
        Spacer(Modifier.height(AppSpacing.lg))
        PeopleRow("Cast", state.cast)
    }
    if (state.crew.isNotEmpty()) {
        Spacer(Modifier.height(AppSpacing.lg))
        PeopleRow("Crew", state.crew)
    }
    if (detailEntries.isNotEmpty()) {
        Spacer(Modifier.height(AppSpacing.lg))
        DetailsSection(detailEntries)
    }
    if (state.similar.isNotEmpty()) {
        Spacer(Modifier.height(AppSpacing.lg))
        SimilarRow(state.similar) { preview ->
            onOpenDetail(preview.toLibraryItem(item))
        }
    }
    Spacer(Modifier.height(AppSpacing.xl))
}

@Composable
private fun DetailBackdrop(
    state: DetailUiState,
    fade: Float,
) {
    val backdrop = state.background ?: state.poster
    Box(Modifier.fillMaxSize().blur(MAX_BLUR * fade)) {
        // Dark base so a slow or failed image never shows a bright gap; the
        // artwork fills the box on top when it can.
        Box(Modifier.fillMaxSize().background(AppScrim.detailBackdrop))
        if (!backdrop.isNullOrEmpty()) {
            AsyncImage(
                model = backdrop,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
        Box(Modifier.fillMaxSize().background(AppScrim.mediaHorizontal))
        Box(Modifier.fillMaxSize().background(AppScrim.mediaBottom))
        // Scroll-linked darkening and a light blur. It covers the whole
        // viewport, so the artwork stays behind the content all the way down,
        // never fully black.
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = HERO_FADE_MAX * fade)))
    }
}

@Composable
private fun HeroContent(
    state: DetailUiState,
    height: Dp,
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
    Box(Modifier.fillMaxWidth().height(height)) {
        Column(
            modifier =
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(AppSpacing.xl)
                    .widthIn(max = 720.dp),
        ) {
            MediaTitle(state.name, state.logo)
            MetadataRow(detailMetadataParts(state))
            Spacer(Modifier.height(AppSpacing.lg))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xl),
            ) {
                PillButton(text = "Play", icon = Icons.Default.PlayArrow, onClick = onPlay)
                ActionRow(
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
            state.description?.takeIf { it.isNotEmpty() }?.let {
                Spacer(Modifier.height(AppSpacing.lg))
                Text(
                    it,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyLarge,
                    color = AppColors.textPrimary.copy(alpha = 0.92f),
                )
            }
            if (state.genres.isNotEmpty()) {
                Spacer(Modifier.height(AppSpacing.sm))
                Text(
                    state.genres.joinToString("  ·  "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.textSecondary,
                )
            }
        }
    }
}

/** Release info, runtime and rating, dot-separated. */
private fun detailMetadataParts(state: DetailUiState): List<String> =
    buildList {
        state.releaseInfo?.takeIf { it.isNotEmpty() }?.let { add(it) }
        state.runtime?.takeIf { it.isNotEmpty() }?.let { add(it) }
        state.imdbRating?.let { add("★ ${ratingText(it)}") }
    }

/** Icon + label actions under the call to action. */
@Composable
private fun ActionRow(
    saved: Boolean,
    savedBusy: Boolean,
    onToggleSaved: () -> Unit,
    watched: Boolean,
    watchedBusy: Boolean,
    onToggleWatched: () -> Unit,
    rating: Int?,
    onRate: () -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xl)) {
        ActionItem(
            icon = if (saved) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            label = "My List",
            active = saved,
            enabled = !savedBusy,
            onClick = onToggleSaved,
        )
        ActionItem(
            icon = if (rating != null) Icons.Filled.Star else Icons.Outlined.StarBorder,
            label = "Rate",
            active = rating != null,
            enabled = true,
            onClick = onRate,
        )
        ActionItem(
            icon = if (watched) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
            label = "Watched",
            active = watched,
            enabled = !watchedBusy,
            onClick = onToggleWatched,
        )
    }
}

@Composable
private fun ActionItem(
    icon: ImageVector,
    label: String,
    active: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val interaction = rememberInteraction()
    Column(
        modifier =
            Modifier
                .clip(RoundedCornerShape(AppRadii.sm))
                .background(interactionHighlight(interaction))
                .clickableSurface(interaction, onClick, enabled = enabled)
                .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = if (active) AppColors.accent else AppColors.textPrimary,
            modifier = Modifier.size(AppIconSize.lg),
        )
        Spacer(Modifier.height(AppSpacing.xs))
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = if (active) AppColors.accent else AppColors.textPrimary,
        )
    }
}

private fun com.misgus.agustream.core.model.MetaPreview.toLibraryItem(from: LibraryItem): LibraryItem =
    LibraryItem(
        id = id,
        contentId = id,
        contentType = type.ifEmpty { from.contentType },
        name = name,
        poster = poster,
        background = background,
        logo = logo,
        description = description,
        releaseInfo = releaseInfo,
        imdbRating = imdbRating,
        genres = genres,
        addonBaseUrl = from.addonBaseUrl,
    )

/** Height of the fixed top scrim, in dp. */
private val TOP_SCRIM_HEIGHT = 96.dp

/** Blur applied to the artwork at the end of the page, in dp. */
private val MAX_BLUR = 14.dp

/** How dark the artwork gets at the end of the page. */
private const val HERO_FADE_MAX = 0.9f
