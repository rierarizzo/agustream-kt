package com.misgus.agustream.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.misgus.agustream.core.model.LibraryItem
import com.misgus.agustream.core.model.MetaVideo
import com.misgus.agustream.core.repository.AccountRepository
import com.misgus.agustream.core.repository.CatalogRepository
import com.misgus.agustream.core.repository.LibraryRepository
import com.misgus.agustream.core.repository.MetadataRepository
import com.misgus.agustream.core.util.runCatchingCancellable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import kotlin.time.Instant

/** One upcoming episode of a series saved in the Library. */
data class CalendarEntry(
    val item: LibraryItem,
    val video: MetaVideo,
    val date: LocalDate,
)

/** Immutable state of the Calendar section. */
data class CalendarUiState(
    val isLoading: Boolean = false,
    val hasLoaded: Boolean = false,
    val error: Throwable? = null,
    val entries: List<CalendarEntry> = emptyList(),
)

/**
 * Upcoming episodes of the series in the Library.
 *
 * Cinemeta (and AIOMetadata) expose a `calendar-videos` catalog that returns
 * the calendar videos of many series in a single request, so that is used when
 * an addon offers it. Otherwise the section falls back to resolving every saved
 * series through the metadata addons. Either way only episodes inside a rolling
 * window are kept, and ones without a release date are skipped.
 */
class CalendarViewModel(
    private val account: AccountRepository,
    private val library: LibraryRepository,
    private val metadata: MetadataRepository,
    private val catalogs: CatalogRepository,
    /** Injectable clock so the window is deterministic in tests. */
    private val today: () -> LocalDate = { Clock.System.todayIn(TimeZone.currentSystemDefault()) },
) : ViewModel() {
    private val _state = MutableStateFlow(CalendarUiState())
    val state: StateFlow<CalendarUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            account.state.collect { accountState ->
                when {
                    !accountState.isSignedIn -> clear()
                    // The library belongs to a profile, so wait for one.
                    accountState.requiresProfile && accountState.activeProfile == null -> Unit
                    else -> load(force = true)
                }
            }
        }
    }

    /** Loads the upcoming episodes. Does nothing when loaded, unless [force]. */
    fun load(force: Boolean = false) {
        if (!account.state.value.isSignedIn) {
            clear()
            return
        }
        if (_state.value.isLoading || (_state.value.hasLoaded && !force)) return
        viewModelScope.launch { doLoad() }
    }

    private suspend fun doLoad() {
        _state.update {
            it.copy(isLoading = true, hasLoaded = false, error = null, entries = emptyList())
        }
        try {
            val start = today()
            val end = start.plus(WINDOW_DAYS, DateTimeUnit.DAY)
            val series =
                library
                    .all()
                    .filter { it.contentType == "series" }
                    .distinctBy { it.contentId }
                    .take(MAX_SERIES)
            if (!loadFromCalendarCatalog(series, start, end)) {
                loadFromMetadata(series, start, end)
            }
            _state.update { it.copy(isLoading = false, hasLoaded = true) }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Throwable) {
            _state.update { it.copy(isLoading = false, hasLoaded = true, error = error) }
        }
    }

    /**
     * Asks a `calendar-videos` catalog for the whole library at once.
     *
     * The catalog answers with `metasDetailed`: the full meta of every series,
     * with only its calendar videos. Returns `true` when an addon produced
     * entries; a catalog that fails or returns nothing falls through to the next
     * one, and to the metadata path afterwards.
     */
    private suspend fun loadFromCalendarCatalog(
        series: List<LibraryItem>,
        start: LocalDate,
        end: LocalDate,
    ): Boolean {
        if (series.isEmpty()) return false
        val refs =
            runCatchingCancellable { catalogs.catalogs() }
                .getOrDefault(emptyList())
                .filter { it.type == "series" && it.extras.any { extra -> extra.name == CALENDAR_IDS_EXTRA } }
        if (refs.isEmpty()) return false
        val byId = series.associateBy { it.contentId }
        val ids = series.map { it.contentId }.filter { it.isNotEmpty() }.distinct()
        if (ids.isEmpty()) return false
        val extra = mapOf(CALENDAR_IDS_EXTRA to ids.joinToString(","))
        for (ref in refs) {
            val details =
                runCatchingCancellable { catalogs.detailedItems(ref, extra) }
                    .getOrDefault(emptyList())
            val entries =
                details.flatMap { detail ->
                    val item = byId[detail.id] ?: return@flatMap emptyList()
                    entriesFor(item, detail.videos, start, end)
                }
            if (entries.isNotEmpty()) {
                _state.update { it.copy(entries = entries.sortedWith(ENTRY_ORDER)) }
                return true
            }
        }
        return false
    }

    /** Fallback: resolve each series through the metadata addons. */
    private suspend fun loadFromMetadata(
        series: List<LibraryItem>,
        start: LocalDate,
        end: LocalDate,
    ) {
        // A semaphore instead of fixed batches: the next series starts as soon
        // as one finishes, with no waiting for the slowest of a batch.
        val semaphore = Semaphore(CONCURRENCY)
        coroutineScope {
            series
                .map { item ->
                    async {
                        semaphore.withPermit {
                            val found =
                                runCatchingCancellable {
                                    val videos = metadata.detail(item.contentType, item.contentId)?.videos.orEmpty()
                                    entriesFor(item, videos, start, end)
                                }.getOrDefault(emptyList())
                            if (found.isNotEmpty()) {
                                // Publish as each series resolves, so the grid
                                // fills in instead of waiting for all of them.
                                _state.update {
                                    it.copy(entries = (it.entries + found).sortedWith(ENTRY_ORDER))
                                }
                            }
                        }
                    }
                }.awaitAll()
        }
    }

    /** The episodes of [item] that air inside `[start, end]`. */
    private fun entriesFor(
        item: LibraryItem,
        videos: List<MetaVideo>,
        start: LocalDate,
        end: LocalDate,
    ): List<CalendarEntry> =
        videos.mapNotNull { video ->
            val date = releaseDateOrNull(video.released) ?: return@mapNotNull null
            if ((video.season ?: 0) <= 0) return@mapNotNull null
            if (date < start || date > end) return@mapNotNull null
            CalendarEntry(item, video, date)
        }

    /** Parses a Stremio `released` value, which may be an ISO instant or a date. */
    private fun releaseDateOrNull(released: String?): LocalDate? {
        if (released.isNullOrEmpty()) return null
        runCatching { return Instant.parse(released).toLocalDateTime(TimeZone.currentSystemDefault()).date }
        runCatching { return LocalDate.parse(released) }
        return null
    }

    private fun clear() {
        _state.value = CalendarUiState()
    }

    private companion object {
        const val WINDOW_DAYS = 180
        const val MAX_SERIES = 60
        const val CONCURRENCY = 8
        const val CALENDAR_IDS_EXTRA = "calendarVideosIds"

        val ENTRY_ORDER =
            compareBy<CalendarEntry>(
                { it.date },
                { it.item.name },
                { it.video.season ?: 0 },
                { it.video.episode ?: 0 },
            )
    }
}
