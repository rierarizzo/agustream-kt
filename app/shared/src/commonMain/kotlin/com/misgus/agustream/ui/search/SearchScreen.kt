package com.misgus.agustream.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.misgus.agustream.core.model.LibraryItem
import com.misgus.agustream.core.model.MetaPreview
import com.misgus.agustream.core.repository.PosterSize
import com.misgus.agustream.ui.components.Message
import com.misgus.agustream.ui.components.MetaPosterCard
import com.misgus.agustream.ui.components.SectionHeader
import com.misgus.agustream.ui.components.toUserMessage
import com.misgus.agustream.ui.services.LocalAppServices
import com.misgus.agustream.ui.shell.ContentNavigator
import com.misgus.agustream.ui.shell.ContentRoute
import com.misgus.agustream.ui.theme.AppSpacing
import com.misgus.agustream.ui.theme.cellWidth
import com.misgus.agustream.ui.theme.railHeight

/** Search section: free-text search across every searchable addon catalog. */
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    navigator: ContentNavigator,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val settingsRepository = LocalAppServices.current.settings
    val settings by settingsRepository.state.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        SearchField(query = state.query, onQueryChange = viewModel::onQueryChange)
        Box(Modifier.weight(1f)) {
            SearchContent(
                state = state,
                posterSize = settings.posterSize,
                onOpenDetail = { navigator.push(ContentRoute.Detail(it)) },
            )
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        placeholder = { Text("Search movies and series") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Clear")
                }
            }
        },
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(start = AppSpacing.xl, end = AppSpacing.xl, top = AppSpacing.lg),
    )
}

@Composable
private fun SearchContent(
    state: SearchUiState,
    posterSize: PosterSize,
    onOpenDetail: (LibraryItem) -> Unit,
) {
    when {
        state.query.isBlank() ->
            Message(
                icon = Icons.Default.Search,
                title = "Search",
                message = "Type a title to search across every installed addon.",
            )
        state.isSearching && state.results.isEmpty() ->
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator()
            }
        state.error != null && state.results.isEmpty() ->
            Message(
                icon = Icons.Outlined.ErrorOutline,
                title = "Search failed",
                message = state.error.toUserMessage(),
            )
        state.results.isEmpty() && state.hasSearched ->
            Message(
                icon = Icons.Outlined.Inbox,
                title = "No results",
                message = "Nothing matched \"${state.query.trim()}\".",
            )
        else -> {
            val movies = state.results.filter { it.type == "movie" }
            val series = state.results.filter { it.type == "series" }
            val other = state.results.filter { it.type != "movie" && it.type != "series" }
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                if (movies.isNotEmpty()) {
                    SectionHeader("Movies")
                    ResultsRail(movies, posterSize, onOpenDetail)
                }
                if (series.isNotEmpty()) {
                    SectionHeader("Series")
                    ResultsRail(series, posterSize, onOpenDetail)
                }
                if (other.isNotEmpty()) {
                    SectionHeader("Other")
                    ResultsRail(other, posterSize, onOpenDetail)
                }
                Spacer(Modifier.height(AppSpacing.xl))
            }
        }
    }
}

@Composable
private fun ResultsRail(
    previews: List<MetaPreview>,
    posterSize: PosterSize,
    onOpenDetail: (LibraryItem) -> Unit,
) {
    LazyRow(
        modifier = Modifier.height(posterSize.railHeight),
        contentPadding = PaddingValues(horizontal = AppSpacing.xl),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        items(previews, key = { "${it.type}:${it.id}" }) { item ->
            MetaPosterCard(
                preview = item,
                width = posterSize.cellWidth,
                onTap = { onOpenDetail(LibraryItem.fromPreview(item)) },
            )
        }
    }
}
