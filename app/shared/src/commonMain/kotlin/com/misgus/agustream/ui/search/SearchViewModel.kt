package com.misgus.agustream.ui.search

import androidx.lifecycle.viewModelScope
import com.misgus.agustream.core.model.CatalogExtras
import com.misgus.agustream.core.model.MetaPreview
import com.misgus.agustream.core.repository.AccountRepository
import com.misgus.agustream.core.repository.CatalogRef
import com.misgus.agustream.core.repository.CatalogRepository
import com.misgus.agustream.core.util.runCatchingCancellable
import com.misgus.agustream.ui.common.AccountScopedViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Immutable state of the Search section. */
data class SearchUiState(
    val query: String = "",
    val isSearching: Boolean = false,
    val results: List<MetaPreview> = emptyList(),
    /** `true` once a query has completed, so "no results" is not shown before. */
    val hasSearched: Boolean = false,
    val error: Throwable? = null,
)

/**
 * Searches every catalog that declares the `search` extra, in parallel.
 *
 * The query is debounced so typing does not fire a request per keystroke, and
 * results are deduped across addons by `type:id`.
 */
class SearchViewModel(
    account: AccountRepository,
    private val catalogs: CatalogRepository,
) : AccountScopedViewModel(account) {
    private val _state = MutableStateFlow(SearchUiState())
    val state: StateFlow<SearchUiState> = _state.asStateFlow()

    private var searchJob: Job? = null

    /** Profile the cached searchable catalogs belong to, or `null` when unset. */
    private var catalogProfile: Int? = null
    private var searchable: List<CatalogRef> = emptyList()

    private var requestId = 0

    override fun onAccountReady() = Unit

    override fun onAccountCleared() = clear()

    /** Updates the query and schedules a debounced search. */
    fun onQueryChange(query: String) {
        _state.update { it.copy(query = query) }
        searchJob?.cancel()
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            requestId++
            _state.update {
                it.copy(isSearching = false, results = emptyList(), hasSearched = false, error = null)
            }
            return
        }
        searchJob =
            viewModelScope.launch {
                delay(DEBOUNCE_MS)
                search(trimmed)
            }
    }

    private suspend fun search(query: String) {
        val id = ++requestId
        _state.update { it.copy(isSearching = true, error = null) }
        try {
            val results =
                coroutineScope {
                    searchableCatalogs()
                        .map { ref ->
                            async {
                                runCatchingCancellable {
                                    catalogs.items(ref, mapOf(CatalogExtras.SEARCH to query))
                                }.getOrDefault(emptyList())
                            }
                        }.awaitAll()
                }.flatten()
            if (id != requestId) return
            val seen = mutableSetOf<String>()
            val unique = results.filter { seen.add("${it.type}:${it.id}") }
            _state.update { it.copy(isSearching = false, results = unique, hasSearched = true) }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Throwable) {
            if (id != requestId) return
            _state.update { it.copy(isSearching = false, error = error, hasSearched = true) }
        }
    }

    /** Searchable catalogs for the active profile, fetched once and cached. */
    private suspend fun searchableCatalogs(): List<CatalogRef> {
        val profile =
            account.state.value.activeProfile
                ?.profileId
        if (profile == catalogProfile && searchable.isNotEmpty()) return searchable
        val refs = catalogs.catalogs().filter { it.searches }
        catalogProfile = profile
        searchable = refs
        return refs
    }

    private fun clear() {
        searchJob?.cancel()
        requestId++
        catalogProfile = null
        searchable = emptyList()
        _state.value = SearchUiState()
    }

    private companion object {
        const val DEBOUNCE_MS = 300L
    }
}
