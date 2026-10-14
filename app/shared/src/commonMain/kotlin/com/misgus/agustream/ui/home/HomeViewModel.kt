package com.misgus.agustream.ui.home

import androidx.lifecycle.viewModelScope
import com.misgus.agustream.core.model.LibraryItem
import com.misgus.agustream.core.model.MetaPreview
import com.misgus.agustream.core.model.ProgressThresholds
import com.misgus.agustream.core.model.WatchProgress
import com.misgus.agustream.core.repository.AccountRepository
import com.misgus.agustream.core.repository.CatalogRef
import com.misgus.agustream.core.repository.CatalogRepository
import com.misgus.agustream.core.repository.LibraryRepository
import com.misgus.agustream.core.repository.ProgressRepository
import com.misgus.agustream.core.util.runCatchingCancellable
import com.misgus.agustream.core.watched.WatchedBadgeResolver
import com.misgus.agustream.core.watched.WatchedCandidate
import com.misgus.agustream.ui.common.AccountScopedViewModel
import com.misgus.agustream.ui.common.WatchedBadgeSync
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Instant

/** A library title with progress, shown in "Continue watching". */
data class ContinueEntry(
    val item: LibraryItem,
    val progress: WatchProgress,
)

/** One catalog row on Home. */
data class HomeRow(
    val ref: CatalogRef,
    val title: String,
    val items: List<MetaPreview>,
)

/** Immutable state of the Home section. */
data class HomeUiState(
    val isLoading: Boolean = false,
    val hasLoaded: Boolean = false,
    val error: Throwable? = null,
    val featured: List<MetaPreview> = emptyList(),
    val continueWatching: List<ContinueEntry> = emptyList(),
    val rows: List<HomeRow> = emptyList(),
    val watchedIds: Set<String> = emptySet(),
    val isSyncingWatched: Boolean = false,
)

/**
 * Loads Home: a featured set, "Continue watching" and catalog rows.
 *
 * Catalogs come from the addons; continue watching comes from the backend
 * (watch progress joined with the library). The watched badges are resolved in
 * the background, so the content paints first.
 */
class HomeViewModel(
    account: AccountRepository,
    private val catalogs: CatalogRepository,
    private val library: LibraryRepository,
    private val progress: ProgressRepository,
    badges: WatchedBadgeResolver,
) : AccountScopedViewModel(account) {
    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    private val watchedSync = WatchedBadgeSync(badges, account)

    override fun onAccountReady() = load(force = true)

    override fun onAccountCleared() = clear()

    /** Loads everything. Does nothing when already loaded, unless [force]. */
    fun load(force: Boolean = false) {
        if (!account.state.value.isSignedIn) {
            clear()
            return
        }
        if (_state.value.isLoading || (_state.value.hasLoaded && !force)) return
        viewModelScope.launch { doLoad() }
    }

    private suspend fun doLoad() {
        _state.update { it.copy(isLoading = true, error = null) }
        var progressEntries: List<WatchProgress> = emptyList()
        var loaded = false
        try {
            // Watch progress, library and the catalog list are independent, so
            // they all start at once instead of paying three round-trips in a row.
            val (entries, libraryItems, refs) =
                coroutineScope {
                    val progressDeferred = async { progress.all() }
                    val libraryDeferred = async { library.all() }
                    val catalogsDeferred = async { catalogs.catalogs().filter { it.browsable }.take(MAX_ROWS) }
                    Triple(progressDeferred.await(), libraryDeferred.await(), catalogsDeferred.await())
                }
            progressEntries = entries
            val byContent = libraryItems.associateBy { it.contentId }
            val continueWatching =
                progressEntries
                    .mapNotNull { entry ->
                        val item = byContent[entry.contentId] ?: return@mapNotNull null
                        val fraction = entry.fraction ?: 0.0
                        if (fraction <= ProgressThresholds.IN_PROGRESS_MIN ||
                            fraction >= ProgressThresholds.IN_PROGRESS_MAX
                        ) {
                            return@mapNotNull null
                        }
                        ContinueEntry(item, entry)
                    }.sortedByDescending { it.progress.lastWatched ?: Instant.fromEpochMilliseconds(0) }
                    .take(MAX_CONTINUE)

            val rows = loadRows(refs)
            _state.update {
                it.copy(
                    isLoading = false,
                    hasLoaded = true,
                    continueWatching = continueWatching,
                    rows = rows,
                    featured = rows.firstOrNull()?.items?.take(MAX_FEATURED) ?: emptyList(),
                )
            }
            loaded = true
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Throwable) {
            _state.update { it.copy(isLoading = false, error = error) }
        }
        // Home paints without waiting for the watch history; the checks fill in
        // when the lookup finishes.
        if (loaded) syncWatched(progressEntries)
    }

    private suspend fun loadRows(refs: List<CatalogRef>): List<HomeRow> =
        coroutineScope {
            refs
                .map { ref ->
                    async {
                        runCatchingCancellable {
                            val items = catalogs.items(ref)
                            if (items.isEmpty()) {
                                null
                            } else {
                                HomeRow(ref, ref.name, items.take(MAX_ITEMS_PER_ROW))
                            }
                        }.getOrNull()
                    }
                }.awaitAll()
                .filterNotNull()
        }

    private suspend fun syncWatched(progressEntries: List<WatchProgress>) {
        val candidates =
            buildSet {
                _state.value.rows.forEach { row -> row.items.forEach { add(WatchedCandidate(it.id, it.type)) } }
                _state.value.continueWatching.forEach {
                    add(WatchedCandidate(it.item.contentId, it.item.contentType))
                }
            }
        watchedSync.resolve(
            candidates = candidates,
            progressEntries = progressEntries,
            onStart = { _state.update { it.copy(isSyncingWatched = true) } },
            onResult = { watched ->
                _state.update { it.copy(watchedIds = watched, isSyncingWatched = false) }
            },
        )
    }

    private fun clear() {
        watchedSync.invalidate()
        _state.value = HomeUiState()
    }

    private companion object {
        const val MAX_ROWS = 5
        const val MAX_ITEMS_PER_ROW = 20
        const val MAX_CONTINUE = 12
        const val MAX_FEATURED = 6
    }
}
