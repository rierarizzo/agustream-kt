package com.misgus.agustream.ui

import com.misgus.agustream.core.model.LibraryItem
import com.misgus.agustream.core.repository.CatalogRef
import com.misgus.agustream.core.watched.WatchedBadgeResolver
import com.misgus.agustream.ui.home.HomeViewModel
import com.misgus.agustream.ui.library.LibraryViewModel
import com.misgus.agustream.ui.support.FakeAccountRepository
import com.misgus.agustream.ui.support.FakeCatalogRepository
import com.misgus.agustream.ui.support.FakeLibraryRepository
import com.misgus.agustream.ui.support.FakeMetadataRepository
import com.misgus.agustream.ui.support.FakeProgressRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LibraryViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun item(
        id: String,
        type: String,
    ) = LibraryItem(id = id, contentId = id, contentType = type, name = id)

    private fun viewModel(library: FakeLibraryRepository): LibraryViewModel {
        val account = FakeAccountRepository()
        val progress = FakeProgressRepository()
        val badges = WatchedBadgeResolver(progress, FakeMetadataRepository())
        return LibraryViewModel(account, library, progress, badges)
    }

    @Test
    fun loadsItems() =
        runTest(dispatcher) {
            val viewModel =
                viewModel(
                    FakeLibraryRepository(listOf(item("m1", "movie"), item("s1", "series"))),
                )
            viewModel.load()
            advanceUntilIdle()

            assertTrue(viewModel.state.value.hasLoaded)
            assertTrue(viewModel.state.value.isSignedIn)
            assertEquals(2, viewModel.state.value.items.size)
        }

    @Test
    fun clearsWhenSignedOut() =
        runTest(dispatcher) {
            val account = FakeAccountRepository()
            val viewModel =
                LibraryViewModel(
                    account,
                    FakeLibraryRepository(listOf(item("m1", "movie"))),
                    FakeProgressRepository(),
                    WatchedBadgeResolver(FakeProgressRepository(), FakeMetadataRepository()),
                )
            viewModel.load()
            advanceUntilIdle()
            assertEquals(1, viewModel.state.value.items.size)

            account.signOut()
            advanceUntilIdle()
            assertEquals(0, viewModel.state.value.items.size)
            assertEquals(false, viewModel.state.value.isSignedIn)
        }
}

class HomeViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun loadsCatalogRowsAndFeatured() =
        runTest(dispatcher) {
            val ref =
                CatalogRef(
                    addonName = "Addon",
                    addonBaseUrl = "https://addon",
                    type = "movie",
                    id = "top",
                    name = "Top",
                )
            val preview =
                com.misgus.agustream.core.model.MetaPreview(
                    id = "tt1",
                    type = "movie",
                    name = "Movie",
                )
            val viewModel =
                HomeViewModel(
                    account = FakeAccountRepository(),
                    catalogs = FakeCatalogRepository(listOf(ref), mapOf(ref.key to listOf(preview))),
                    library = FakeLibraryRepository(),
                    progress = FakeProgressRepository(),
                    badges = WatchedBadgeResolver(FakeProgressRepository(), FakeMetadataRepository()),
                )
            viewModel.load()
            advanceUntilIdle()

            assertTrue(viewModel.state.value.hasLoaded)
            assertTrue(viewModel.state.value.isSignedIn)
            assertEquals(1, viewModel.state.value.rows.size)
            assertEquals(
                "tt1",
                viewModel.state.value.featured
                    .single()
                    .id,
            )
        }
}
