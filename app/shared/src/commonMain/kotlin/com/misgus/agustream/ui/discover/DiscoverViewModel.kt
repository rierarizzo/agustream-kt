package com.misgus.agustream.ui.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.misgus.agustream.core.model.MetaPreview
import com.misgus.agustream.core.repository.AccountRepository
import com.misgus.agustream.core.repository.CatalogRef
import com.misgus.agustream.core.repository.CatalogRepository
import com.misgus.agustream.core.util.runCatchingCancellable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Immutable state of the Discover section. */
data class DiscoverUiState(
    val isSignedIn: Boolean = false,
    /** `true` during the very first load. */
    val isLoading: Boolean = false,
    /** `true` while an additional page is being appended. */
    val isLoadingMore: Boolean = false,
    val hasLoaded: Boolean = false,
    /** Whether scrolling further can still add items. */
    val hasMore: Boolean = true,
    val error: Throwable? = null,
    /** Every catalog item, movies and series mixed like the Library. */
    val items: List<MetaPreview> = emptyList(),
)

/**
 * Flattens every catalog the account's addons expose into a single grid.
 *
 * Unlike Home, Discover does not keep the catalog boundaries: the items are
 * interleaved round-robin across catalogs and deduped, so the grid alternates
 * sources instead of showing one addon's catalog as a block. The grid pages
 * like a feed: each [loadMore] asks every catalog that declares `skip` for its
 * next page, so scrolling keeps revealing more instead of stopping at the first
 * page.
 */
class DiscoverViewModel(
    private val account: AccountRepository,
    private val catalogs: CatalogRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(DiscoverUiState())
    val state: StateFlow<DiscoverUiState> = _state.asStateFlow()

    private var refs: List<CatalogRef> = emptyList()

    /** Next `skip` per catalog, keyed by [CatalogRef.key]. */
    private val offsets = mutableMapOf<String, Int>()

    /** Catalogs that returned an empty page (or ignored `skip`), so they are done. */
    private val exhausted = mutableSetOf<String>()

    /** Cross-catalog dedupe, keyed by `type:id`. */
    private val seen = mutableSetOf<String>()
    private val items = mutableListOf<MetaPreview>()

    private var loadJob: Job? = null

    init {
        viewModelScope.launch {
            account.state.collect { accountState ->
                when {
                    !accountState.isSignedIn -> clear()
                    // The addons belong to a profile, so wait for one.
                    accountState.requiresProfile && accountState.activeProfile == null -> Unit
                    else -> load(force = true)
                }
            }
        }
    }

    /** Loads the first page of every catalog. Does nothing when loaded, unless [force]. */
    fun load(force: Boolean = false) {
        if (!account.state.value.isSignedIn) {
            clear()
            return
        }
        if (_state.value.isLoading || _state.value.isLoadingMore) return
        if (_state.value.hasLoaded && !force) return
        loadJob?.cancel()
        loadJob = viewModelScope.launch { initialLoad() }
    }

    /** Appends the next page of every catalog that still has one. */
    fun loadMore() {
        val current = _state.value
        if (current.isLoading || current.isLoadingMore || !current.hasLoaded || !current.hasMore) return
        if (!account.state.value.isSignedIn) return
        loadJob = viewModelScope.launch { appendPage() }
    }

    private suspend fun initialLoad() {
        _state.update { it.copy(isSignedIn = true, isLoading = true, error = null, hasMore = true) }
        try {
            // `catalogs()` returns the catalogs grouped per addon; reorder them so
            // an addon that declares many does not crowd out the rest when the
            // list is truncated.
            refs = diversify(catalogs.catalogs().filter { it.browsable }.distinctBy { it.key }).take(MAX_CATALOGS)
            offsets.clear()
            exhausted.clear()
            seen.clear()
            items.clear()
            fetchPage()
            _state.update {
                it.copy(isLoading = false, hasLoaded = true, items = items.toList(), hasMore = moreAvailable())
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Throwable) {
            _state.update { it.copy(isLoading = false, hasLoaded = true, error = error) }
        }
    }

    private suspend fun appendPage() {
        _state.update { it.copy(isLoadingMore = true) }
        try {
            fetchPage()
            _state.update { it.copy(isLoadingMore = false, items = items.toList(), hasMore = moreAvailable()) }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Throwable) {
            _state.update { it.copy(isLoadingMore = false, hasMore = false) }
        }
    }

    /** One page from every catalog that is not done yet, in parallel. */
    private suspend fun fetchPage() {
        val active = refs.filter { it.key !in exhausted }
        if (active.isEmpty()) return
        val results =
            coroutineScope {
                active
                    .map { ref ->
                        async {
                            val skip = offsets[ref.key] ?: 0
                            val extra = if (skip == 0) null else mapOf("skip" to skip.toString())
                            ref to runCatchingCancellable { catalogs.items(ref, extra) }
                        }
                    }.awaitAll()
            }
        val freshPerCatalog = mutableListOf<List<MetaPreview>>()
        for ((ref, result) in results) {
            // A transient failure is not the end of the catalog: leave it for
            // the next loadMore instead of dropping it.
            val page = result.getOrNull() ?: continue
            if (page.isEmpty()) {
                exhausted += ref.key
                continue
            }
            offsets[ref.key] = (offsets[ref.key] ?: 0) + page.size
            // A catalog without `skip` (or one that ignores it and repeats the
            // page) has nothing beyond the first page.
            if (!ref.extras.any { it.name == "skip" }) exhausted += ref.key
            val fresh = page.filter { seen.add("${it.type}:${it.id}") }
            if (fresh.isEmpty()) {
                exhausted += ref.key
            } else {
                freshPerCatalog += fresh
            }
        }
        // Round-robin so consecutive items come from different catalogs: the
        // grid never turns into a block of a single addon's catalog.
        items += interleave(freshPerCatalog)
        if (items.size >= MAX_ITEMS) refs.forEach { exhausted += it.key }
    }

    /** One item from each list in turn, then the second of each, and so on. */
    private fun interleave(lists: List<List<MetaPreview>>): List<MetaPreview> {
        if (lists.size <= 1) return lists.firstOrNull().orEmpty()
        val result = ArrayList<MetaPreview>(lists.sumOf { it.size })
        var index = 0
        var advanced = true
        while (advanced) {
            advanced = false
            for (list in lists) {
                if (index < list.size) {
                    result += list[index]
                    advanced = true
                }
            }
            index++
        }
        return result
    }

    /** Alternates catalogs across addons, keeping the account's addon order. */
    private fun diversify(all: List<CatalogRef>): List<CatalogRef> {
        val byAddon = all.groupBy { it.addonBaseUrl }
        if (byAddon.size <= 1) return all
        val result = ArrayList<CatalogRef>(all.size)
        var index = 0
        var advanced = true
        while (advanced) {
            advanced = false
            for (list in byAddon.values) {
                if (index < list.size) {
                    result += list[index]
                    advanced = true
                }
            }
            index++
        }
        return result
    }

    private fun moreAvailable(): Boolean = items.size < MAX_ITEMS && refs.any { it.key !in exhausted }

    private fun clear() {
        loadJob?.cancel()
        refs = emptyList()
        offsets.clear()
        exhausted.clear()
        seen.clear()
        items.clear()
        _state.value = DiscoverUiState()
    }

    private companion object {
        const val MAX_CATALOGS = 20
        const val MAX_ITEMS = 2000
    }
}
