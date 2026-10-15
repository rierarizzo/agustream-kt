package com.misgus.pengu.ui.calendar

import com.misgus.pengu.core.model.AddonCatalogExtra
import com.misgus.pengu.core.model.LibraryItem
import com.misgus.pengu.core.model.MetaDetail
import com.misgus.pengu.core.model.MetaVideo
import com.misgus.pengu.core.repository.CatalogRef
import com.misgus.pengu.ui.support.FakeAccountRepository
import com.misgus.pengu.ui.support.FakeCatalogRepository
import com.misgus.pengu.ui.support.FakeLibraryRepository
import com.misgus.pengu.ui.support.FakeMetadataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class CalendarViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun keepsOnlyAiredSeasonEpisodesInsideTheWindow() =
        runTest(dispatcher) {
            val series =
                LibraryItem(id = "s1", contentId = "tt1", contentType = "series", name = "Show")
            val detail =
                MetaDetail(
                    id = "tt1",
                    type = "series",
                    name = "Show",
                    videos =
                        listOf(
                            MetaVideo(id = "e1", title = "Soon", season = 1, episode = 1, released = "2025-06-01"),
                            MetaVideo(id = "e2", title = "Past", season = 1, episode = 2, released = "2025-05-01"),
                            MetaVideo(id = "e3", title = "Unknown", season = 1, episode = 3, released = null),
                            MetaVideo(id = "s0", title = "Special", season = 0, episode = 1, released = "2025-06-02"),
                            MetaVideo(id = "e4", title = "Too far", season = 2, episode = 1, released = "2026-01-01"),
                        ),
                )
            val viewModel =
                CalendarViewModel(
                    account = FakeAccountRepository(),
                    library = FakeLibraryRepository(listOf(series)),
                    metadata = FakeMetadataRepository(details = mapOf("tt1" to detail)),
                    catalogs = FakeCatalogRepository(),
                    today = { LocalDate(2025, 5, 12) },
                )
            advanceUntilIdle()

            assertEquals(
                listOf("e1"),
                viewModel.state.value.entries
                    .map { it.video.id },
            )
            assertEquals(
                LocalDate(2025, 6, 1),
                viewModel.state.value.entries
                    .first()
                    .date,
            )
        }

    @Test
    fun usesTheCalendarCatalogWhenAnAddonExposesIt() =
        runTest(dispatcher) {
            val series =
                LibraryItem(id = "s1", contentId = "tt1", contentType = "series", name = "Show")
            val ref =
                CatalogRef(
                    addonName = "Cinemeta",
                    addonBaseUrl = "https://cinemeta",
                    type = "series",
                    id = "calendar-videos",
                    name = "Calendar videos",
                    extras = listOf(AddonCatalogExtra("calendarVideosIds", isRequired = true)),
                )
            val detail =
                MetaDetail(
                    id = "tt1",
                    type = "series",
                    name = "Show",
                    videos = listOf(MetaVideo(id = "e1", title = "Soon", season = 1, episode = 1, released = "2025-06-01")),
                )
            val viewModel =
                CalendarViewModel(
                    account = FakeAccountRepository(),
                    library = FakeLibraryRepository(listOf(series)),
                    // The metadata repository returns nothing, so the entry can
                    // only come from the calendar catalog.
                    metadata = FakeMetadataRepository(),
                    catalogs = FakeCatalogRepository(refs = listOf(ref), detailed = mapOf(ref.key to listOf(detail))),
                    today = { LocalDate(2025, 5, 12) },
                )
            advanceUntilIdle()

            assertEquals(
                listOf("e1"),
                viewModel.state.value.entries
                    .map { it.video.id },
            )
        }
}
