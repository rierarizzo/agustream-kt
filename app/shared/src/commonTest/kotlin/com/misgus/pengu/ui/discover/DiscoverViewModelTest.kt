package com.misgus.pengu.ui.discover

import com.misgus.pengu.core.model.AddonCatalogExtra
import com.misgus.pengu.core.model.MetaPreview
import com.misgus.pengu.core.repository.CatalogRef
import com.misgus.pengu.core.repository.CatalogRepository
import com.misgus.pengu.ui.support.FakeAccountRepository
import com.misgus.pengu.ui.support.FakeCatalogRepository
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
        addon: String = "addon",
        extras: List<AddonCatalogExtra> = emptyList(),
    ) = CatalogRef(
        addonName = addon,
        addonBaseUrl = "https://$addon",
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
            // m1 comes from the first catalog and m3 from the second; the second
            // catalog's duplicate of m1 is dropped, and round-robin keeps an item
            // of the second catalog before the first catalog's second item.
            assertEquals(listOf("m1", "m3", "m2"), ids)
        }

    @Test
    fun interleavesItemsFromDifferentCatalogs() =
        runTest(dispatcher) {
            val first = ref("movie", "top")
            val second = ref("movie", "popular")
            val catalogs =
                FakeCatalogRepository(
                    refs = listOf(first, second),
                    items =
                        mapOf(
                            first.key to listOf(preview("a1", "movie"), preview("a2", "movie"), preview("a3", "movie")),
                            second.key to listOf(preview("b1", "movie"), preview("b2", "movie")),
                        ),
                )
            val viewModel = DiscoverViewModel(FakeAccountRepository(), catalogs)
            advanceUntilIdle()

            val ids =
                viewModel.state.value.items
                    .map { it.id }
            assertEquals(listOf("a1", "b1", "a2", "b2", "a3"), ids)
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

    @Test
    fun keepsEveryAddonWhenOneDeclaresManyCatalogs() =
        runTest(dispatcher) {
            // The first addon declares far more catalogs than the cap, so a plain
            // take() would drop the second addon entirely.
            val many = (1..25).map { ref("movie", "a$it", addon = "A") }
            val few = (1..2).map { ref("movie", "b$it", addon = "B") }
            val refs = many + few
            val catalogs =
                FakeCatalogRepository(
                    refs = refs,
                    items = refs.associate { it.key to listOf(preview(it.id, "movie")) },
                )
            val viewModel = DiscoverViewModel(FakeAccountRepository(), catalogs)
            advanceUntilIdle()

            val ids =
                viewModel.state.value.items
                    .map { it.id }
            assertTrue(ids.any { it.startsWith("b") })
        }

    @Test
    fun skipsCatalogsThatRequireAnExtra() =
        runTest(dispatcher) {
            val browsable = ref("movie", "top")
            val searchOnly =
                ref("movie", "search", extras = listOf(AddonCatalogExtra("search", isRequired = true)))
            val catalogs =
                FakeCatalogRepository(
                    refs = listOf(searchOnly, browsable),
                    items =
                        mapOf(
                            searchOnly.key to listOf(preview("s1", "movie")),
                            browsable.key to listOf(preview("m1", "movie")),
                        ),
                )
            val viewModel = DiscoverViewModel(FakeAccountRepository(), catalogs)
            advanceUntilIdle()

            val ids =
                viewModel.state.value.items
                    .map { it.id }
            assertEquals(listOf("m1"), ids)
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
