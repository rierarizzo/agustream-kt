package com.misgus.agustream.ui.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.misgus.agustream.core.model.LibraryItem
import com.misgus.agustream.ui.components.Message
import com.misgus.agustream.ui.components.PosterTile
import com.misgus.agustream.ui.components.SyncingIndicator
import com.misgus.agustream.ui.components.toUserMessage
import com.misgus.agustream.ui.shell.ContentNavigator
import com.misgus.agustream.ui.shell.ContentRoute
import com.misgus.agustream.ui.theme.AppSpacing

/** Library section: every title saved in the account. */
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    navigator: ContentNavigator,
    onOpenSettings: () -> Unit,
) {
    LaunchedEffect(Unit) { viewModel.load() }
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().padding(AppSpacing.xl)) {
        Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f)) {
                Text("Library", style = MaterialTheme.typography.headlineLarge)
                Text(subtitle(state), style = MaterialTheme.typography.bodyMedium)
            }
            if (state.isSyncingWatched) {
                SyncingIndicator()
                Spacer(Modifier.size(AppSpacing.sm))
            }
            if (state.isSignedIn) {
                IconButton(
                    enabled = !state.isLoading,
                    onClick = { viewModel.load(force = true) },
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                }
            }
        }
        Spacer(Modifier.height(AppSpacing.md))
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            LibraryFilter.entries.forEach { filter ->
                FilterChip(
                    selected = state.filter == filter,
                    onClick = { viewModel.setFilter(filter) },
                    label = { Text(filter.label) },
                )
            }
        }
        Spacer(Modifier.height(AppSpacing.lg))
        LibraryContent(
            state = state,
            onOpenSettings = onOpenSettings,
            onOpen = { navigator.push(ContentRoute.Detail(it)) },
        )
    }
}

@Composable
private fun LibraryContent(
    state: LibraryUiState,
    onOpenSettings: () -> Unit,
    onOpen: (LibraryItem) -> Unit,
) {
    when {
        !state.isSignedIn ->
            Message(
                icon = Icons.Outlined.Lock,
                title = "Not signed in",
                message = "Sign in to load your library.",
                actionLabel = "Sign in",
                onAction = onOpenSettings,
            )
        state.isLoading && !state.hasLoaded ->
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator()
            }
        state.error != null && !state.hasLoaded ->
            Message(
                icon = Icons.Outlined.ErrorOutline,
                title = "Could not load the library",
                message = state.error.toUserMessage(),
            )
        state.visibleItems.isEmpty() ->
            Message(
                icon = Icons.Outlined.Inbox,
                title = "Nothing to show",
                message =
                    if (state.items.isEmpty()) {
                        "Your library is empty."
                    } else {
                        "No titles match this filter."
                    },
            )
        else ->
            LazyVerticalGrid(
                columns = GridCells.Adaptive(230.dp),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
            ) {
                items(state.visibleItems, key = { it.contentId }) { item ->
                    Box(Modifier.aspectRatio(0.58f)) {
                        PosterTile(
                            item = item,
                            progress = state.progressByContentId[item.contentId],
                            watched = item.contentId in state.watchedIds,
                            onTap = { onOpen(item) },
                        )
                    }
                }
            }
    }
}

private fun subtitle(state: LibraryUiState): String =
    when {
        !state.isSignedIn -> "Sign in to see your library"
        state.isLoading && !state.hasLoaded -> "Loading…"
        state.items.size == 1 -> "1 title"
        else -> "${state.items.size} titles"
    }
