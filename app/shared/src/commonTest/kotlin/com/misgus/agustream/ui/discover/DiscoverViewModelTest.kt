package com.misgus.agustream.ui.discover

import com.misgus.agustream.core.model.AddonCatalogExtra
import com.misgus.agustream.core.model.MetaPreview
import com.misgus.agustream.core.repository.CatalogRef
import com.misgus.agustream.core.repository.CatalogRepository
import com.misgus.agustream.ui.support.FakeAccountRepository
import com.misgus.agustream.ui.support.FakeCatalogRepository
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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DiscoverViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun ref(
        type: String,
        id: String,
        extras: List<AddonCatalogExtra> = emptyList(),
    ) = CatalogRef(
        addonName = "Addon",
        addonBaseUrl = "https://addon",
        type = type,
        id = id,
        name = id,
        extras = extras,
    )

    private fun preview(
        id: String,
        type: String,
    ) = MetaPreview(id = id, type = type, name = id)

    @Test
    fun mixesMoviesAndSeriesInOneList() =
        runTest(dispatcher) {
            val movies = ref("movie", "top")
            val series = ref("series", "trending")
            val catalogs =
                FakeCatalogRepository(
                    refs = listOf(movies, series),
                    items =
                        mapOf(
                            movies.key to listOf(preview("m1", "movie")),
                            series.key to listOf(preview("s1", "series")),
                        ),
                )
            val viewModel = DiscoverViewModel(FakeAccountRepository(), catalogs)
            advanceUntilIdle()

            val ids =
                viewModel.state.value.items
                    .map { it.id }
            assertEquals(listOf("m1", "s1"), ids)
        }

    @Test
    fun dedupesAcrossCatalogsAndSkipsEmptyOnes() =
        runTest(dispatcher) {
            val first = ref("movie", "top")
            val second = ref("movie", "popular")
            val empty = ref("series", "empty")
            val catalogs =
                FakeCatalogRepository(
                    refs = listOf(first, second, empty),
                    items =
                        mapOf(
                            first.key to listOf(preview("m1", "movie"), preview("m2", "movie")),
                            second.key to listOf(preview("m1", "movie"), preview("m3", "movie")),
                        ),
                )
            val viewModel = DiscoverViewModel(FakeAccountRepository(), catalogs)
            advanceUntilIdle()

            val ids =
                viewModel.state.value.items
                    .map { it.id }
            assertEquals(listOf("m1", "m2", "m3"), ids)
        }

    @Test
    fun keepsPagingOnLoadMoreUntilTheCatalogIsExhausted() =
        runTest(dispatcher) {
            val paged = ref("movie", "top", extras = listOf(AddonCatalogExtra("skip")))
            val pages =
                listOf(
                    listOf(preview("m1", "movie"), preview("m2", "movie")),
                    listOf(preview("m3", "movie"), preview("m4", "movie")),
                )
            val catalogs = PagedCatalogRepository(paged, pages, pageSize = 2)
            val viewModel = DiscoverViewModel(FakeAccountRepository(), catalogs)
            advanceUntilIdle()

            val firstPage =
                viewModel.state.value.items
                    .map { it.id }
            assertEquals(listOf("m1", "m2"), firstPage)
            assertTrue(viewModel.state.value.hasMore)

            viewModel.loadMore()
            advanceUntilIdle()
            val secondPage =
                viewModel.state.value.items
                    .map { it.id }
            assertEquals(listOf("m1", "m2", "m3", "m4"), secondPage)

            viewModel.loadMore()
            advanceUntilIdle()
            assertFalse(viewModel.state.value.hasMore)
            assertEquals(4, viewModel.state.value.items.size)
        }

    /** Serves fixed-size pages by translating the `skip` extra into a page index. */
    private class PagedCatalogRepository(
        private val ref: CatalogRef,
        private val pages: List<List<MetaPreview>>,
        private val pageSize: Int,
    ) : CatalogRepository {
        override suspend fun catalogs(): List<CatalogRef> = listOf(ref)

        override suspend fun items(
            ref: CatalogRef,
            extra: Map<String, String>?,
        ): List<MetaPreview> {
            val index = (extra?.get("skip")?.toIntOrNull() ?: 0) / pageSize
            return pages.getOrNull(index).orEmpty()
        }
    }
}
