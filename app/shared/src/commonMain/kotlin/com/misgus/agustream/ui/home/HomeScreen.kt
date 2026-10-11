package com.misgus.agustream.ui.home

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.misgus.agustream.core.model.LibraryItem
import com.misgus.agustream.core.model.MetaPreview
import com.misgus.agustream.ui.components.Message
import com.misgus.agustream.ui.components.MetaPosterCard
import com.misgus.agustream.ui.components.PosterImage
import com.misgus.agustream.ui.components.SyncingIndicator
import com.misgus.agustream.ui.shell.ContentNavigator
import com.misgus.agustream.ui.shell.ContentRoute
import com.misgus.agustream.ui.streams.StreamsRequest
import com.misgus.agustream.ui.theme.AppColors
import com.misgus.agustream.ui.theme.AppRadii
import com.misgus.agustream.ui.theme.AppSpacing
import com.misgus.agustream.ui.theme.PosterCaption

/** Home section: a featured carousel, "Continue watching" and catalog rows. */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    navigator: ContentNavigator,
    onOpenStreams: (StreamsRequest) -> Unit,
) {
    LaunchedEffect(Unit) { viewModel.load() }
    val state by viewModel.state.collectAsStateWithLifecycle()

    Box(Modifier.fillMaxSize()) {
        HomeContent(
            state = state,
            onOpenDetail = { navigator.push(ContentRoute.Detail(it)) },
            onOpenPreview = { navigator.push(ContentRoute.Detail(LibraryItem.fromPreview(it))) },
            onOpenCatalog = { navigator.push(ContentRoute.Catalog(it)) },
            onPlayPreview = { preview ->
                onOpenStreams(
                    StreamsRequest(
                        type = preview.type,
                        id = preview.id,
                        title = preview.name,
                        year = yearOf(preview.releaseInfo),
                        background = preview.background,
                    ),
                )
            },
        )
        if (state.isSyncingWatched) {
            Box(Modifier.align(Alignment.TopEnd).padding(top = AppSpacing.md, end = AppSpacing.xl)) {
                SyncingIndicator()
            }
        }
    }
}

@Composable
private fun HomeContent(
    state: HomeUiState,
    onOpenDetail: (LibraryItem) -> Unit,
    onOpenPreview: (MetaPreview) -> Unit,
    onOpenCatalog: (com.misgus.agustream.core.repository.CatalogRef) -> Unit,
    onPlayPreview: (MetaPreview) -> Unit,
) {
    when {
        !state.isSignedIn ->
            Message(
                icon = Icons.Outlined.Lock,
                title = "Not signed in",
                message = "Sign in from Settings to see your Home.",
            )
        state.isLoading && !state.hasLoaded ->
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator()
            }
        state.error != null && state.rows.isEmpty() && state.continueWatching.isEmpty() ->
            Message(
                icon = Icons.Outlined.ErrorOutline,
                title = "Could not load Home",
                message = state.error.toString(),
            )
        state.rows.isEmpty() && state.continueWatching.isEmpty() ->
            Message(
                icon = Icons.Outlined.Explore,
                title = "Nothing to show",
                message = "Install a metadata addon with catalogs to fill this Home.",
            )
        else ->
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            ) {
                if (state.featured.isNotEmpty()) {
                    Hero(items = state.featured, onOpen = onOpenPreview, onPlay = onPlayPreview)
                }
                if (state.continueWatching.isNotEmpty()) {
                    SectionHeader(title = "Continue watching")
                    LazyRow(
                        modifier = Modifier.height(248.dp),
                        contentPadding = PaddingValues(horizontal = AppSpacing.xl),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
                    ) {
                        items(state.continueWatching) { entry ->
                            ContinueCard(entry) { onOpenDetail(entry.item) }
                        }
                    }
                }
                state.rows.forEach { row ->
                    SectionHeader(title = row.title, onSeeAll = { onOpenCatalog(row.ref) })
                    LazyRow(
                        modifier = Modifier.height(332.dp),
                        contentPadding = PaddingValues(horizontal = AppSpacing.xl),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
                    ) {
                        items(row.items) { item ->
                            MetaPosterCard(
                                preview = item,
                                width = 180.dp,
                                watched = item.id in state.watchedIds,
                                onTap = { onOpenPreview(item) },
                            )
                        }
                    }
                }
                Spacer(Modifier.height(AppSpacing.xl))
            }
    }
}

@Composable
private fun ContinueCard(
    entry: ContinueEntry,
    onTap: () -> Unit,
) {
    val item = entry.item
    val progress = entry.progress
    val episode =
        if (progress.season == null || progress.episode == null) {
            null
        } else {
            "S${progress.season}E${progress.episode}"
        }
    val backdrop = item.background ?: item.poster

    Column(
        modifier = Modifier.width(336.dp).clickable(onClick = onTap),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(AppRadii.md)),
        ) {
            PosterImage(backdrop, Modifier.fillMaxSize())
            progress.fraction?.let { fraction ->
                LinearProgressIndicator(
                    progress = { fraction.toFloat() },
                    color = AppColors.accent,
                    trackColor = AppColors.mediaScrim,
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(3.dp),
                )
            }
        }
        Spacer(Modifier.height(AppSpacing.sm))
        Text(
            item.name,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = PosterCaption.title(),
        )
        episode?.let { Text(it, style = PosterCaption.meta()) }
    }
}

@Composable
internal fun SectionHeader(
    title: String,
    onSeeAll: (() -> Unit)? = null,
) {
    Row(
        modifier =
            Modifier.fillMaxWidth().padding(
                start = AppSpacing.xl,
                end = AppSpacing.xl,
                top = AppSpacing.lg,
                bottom = AppSpacing.md,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
        onSeeAll?.let {
            TextButton(onClick = it) {
                Text("See all")
                Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        }
    }
}

/** Extracts the four-digit year from a Stremio `releaseInfo` string. */
internal fun yearOf(releaseInfo: String?): String? = Regex("(?:1[89]\\d\\d|20\\d\\d)").find(releaseInfo ?: "")?.value
