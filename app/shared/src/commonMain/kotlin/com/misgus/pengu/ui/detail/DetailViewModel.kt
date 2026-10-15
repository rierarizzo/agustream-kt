package com.misgus.pengu.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.misgus.pengu.core.model.ContentTypes
import com.misgus.pengu.core.model.LibraryItem
import com.misgus.pengu.core.model.MetaDetail
import com.misgus.pengu.core.model.MetaPerson
import com.misgus.pengu.core.model.MetaPreview
import com.misgus.pengu.core.model.MetaVideo
import com.misgus.pengu.core.model.releaseYear
import com.misgus.pengu.core.repository.MetadataRepository
import com.misgus.pengu.core.util.runCatchingCancellable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** A role a crew member is credited with. */
enum class CrewRole {
    DIRECTOR,
    WRITER,
}

/** A crew member and the roles they are credited with. */
data class CrewMember(
    val name: String,
    val roles: Set<CrewRole>,
)

/** Merged, render-ready state of the detail screen. */
data class DetailUiState(
    val isLoading: Boolean = false,
    val hasLoaded: Boolean = false,
    val error: Throwable? = null,
    val isSeries: Boolean = false,
    val name: String = "",
    val poster: String? = null,
    val background: String? = null,
    val logo: String? = null,
    val description: String? = null,
    val releaseInfo: String? = null,
    val imdbRating: Double? = null,
    val genres: List<String> = emptyList(),
    val runtime: String? = null,
    val released: String? = null,
    val country: String? = null,
    val cast: List<MetaPerson> = emptyList(),
    val crew: List<CrewMember> = emptyList(),
    val episodes: List<MetaVideo> = emptyList(),
    val similar: List<MetaPreview> = emptyList(),
    val year: Int? = null,
)

/**
 * Loads the metadata for one [LibraryItem] and merges it with what the library
 * already knows.
 *
 * The library item is the immediate fallback, so the hero renders while the
 * addon response is in flight.
 */
class DetailViewModel(
    private val metadata: MetadataRepository,
    val item: LibraryItem,
) : ViewModel() {
    private val _state = MutableStateFlow(initialState())
    val state: StateFlow<DetailUiState> = _state.asStateFlow()

    fun load(force: Boolean = false) {
        if (_state.value.isLoading || (_state.value.hasLoaded && !force)) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val meta =
                try {
                    metadata.detail(item.contentType, item.contentId)
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (error: Throwable) {
                    _state.update { it.copy(isLoading = false, hasLoaded = true, error = error) }
                    return@launch
                }
            _state.value = merge(meta)
            loadSimilar(meta)
        }
    }

    private suspend fun loadSimilar(meta: MetaDetail?) {
        if (meta == null) return
        val similar =
            runCatchingCancellable {
                metadata.similar(
                    type = item.contentType,
                    id = item.contentId,
                    genre = meta.genres.firstOrNull(),
                )
            }.getOrDefault(emptyList())
        _state.update { it.copy(similar = similar) }
    }

    private fun merge(meta: MetaDetail?): DetailUiState {
        val cast = meta?.cast ?: emptyList()
        val crew = buildCrew(meta)
        val year = releaseYear(meta?.releaseInfo ?: item.releaseInfo)
        return DetailUiState(
            isLoading = false,
            hasLoaded = true,
            error = _state.value.error,
            isSeries = item.contentType == ContentTypes.SERIES,
            name = nonEmpty(meta?.name) ?: item.name,
            poster = nonEmpty(meta?.poster) ?: item.poster,
            background = nonEmpty(meta?.background) ?: item.background,
            logo = nonEmpty(meta?.logo) ?: item.logo,
            description = nonEmpty(meta?.description) ?: item.description,
            releaseInfo = nonEmpty(meta?.releaseInfo) ?: item.releaseInfo,
            imdbRating = meta?.imdbRating ?: item.imdbRating,
            genres = meta?.genres?.takeIf { it.isNotEmpty() } ?: item.genres,
            runtime = nonEmpty(meta?.runtime),
            released = nonEmpty(meta?.released),
            country = nonEmpty(meta?.country),
            cast = cast,
            crew = crew,
            episodes = meta?.videos?.filter { it.season != null } ?: emptyList(),
            similar = _state.value.similar,
            year = year,
        )
    }

    private fun initialState(): DetailUiState {
        val year = releaseYear(item.releaseInfo)
        return DetailUiState(
            isSeries = item.contentType == ContentTypes.SERIES,
            name = item.name,
            poster = item.poster,
            background = item.background,
            logo = item.logo,
            description = item.description,
            releaseInfo = item.releaseInfo,
            imdbRating = item.imdbRating,
            genres = item.genres,
            year = year,
        )
    }

    private fun buildCrew(meta: MetaDetail?): List<CrewMember> {
        if (meta == null) return emptyList()
        val roles = linkedMapOf<String, MutableSet<CrewRole>>()
        meta.director.forEach { roles.getOrPut(it) { mutableSetOf() }.add(CrewRole.DIRECTOR) }
        meta.writer.forEach { roles.getOrPut(it) { mutableSetOf() }.add(CrewRole.WRITER) }
        return roles.map { (name, values) -> CrewMember(name = name, roles = values) }
    }

    private fun nonEmpty(value: String?): String? = value?.takeIf { it.isNotEmpty() }
}
