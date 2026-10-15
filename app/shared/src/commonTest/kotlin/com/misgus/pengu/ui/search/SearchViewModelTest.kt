package com.misgus.pengu.ui.search

import com.misgus.pengu.core.model.AddonCatalogExtra
import com.misgus.pengu.core.model.MetaPreview
import com.misgus.pengu.core.repository.CatalogRef
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

class SearchViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun ref(
        id: String,
        searchable: Boolean,
    ) = CatalogRef(
        addonName = "Addon",
        addonBaseUrl = "https://addon-$id",
        type = "movie",
        id = id,
        name = id,
        extras = if (searchable) listOf(AddonCatalogExtra("search")) else emptyList(),
    )

    private fun preview(id: String) = MetaPreview(id = id, type = "movie", name = id)

    @Test
    fun searchesOnlySearchableCatalogsAndDedupes() =
        runTest(dispatcher) {
            val first = ref("first", searchable = true)
            val second = ref("second", searchable = true)
            val plain = ref("top", searchable = false)
            val catalogs =
                FakeCatalogRepository(
                    refs = listOf(first, second, plain),
                    items =
                        mapOf(
                            first.key to listOf(preview("tt1"), preview("tt2")),
                            second.key to listOf(preview("tt1"), preview("tt3")),
                            plain.key to listOf(preview("tt9")),
                        ),
                )
            val viewModel = SearchViewModel(FakeAccountRepository(), catalogs)
            advanceUntilIdle()

            viewModel.onQueryChange("dune")
            advanceUntilIdle()

            assertTrue(viewModel.state.value.hasSearched)
            assertFalse(viewModel.state.value.isSearching)
            assertEquals(
                listOf("tt1", "tt2", "tt3"),
                viewModel.state.value.results
                    .map { it.id },
            )
        }

    @Test
    fun clearingTheQueryResetsResults() =
        runTest(dispatcher) {
            val searchable = ref("first", searchable = true)
            val catalogs =
                FakeCatalogRepository(
                    refs = listOf(searchable),
                    items = mapOf(searchable.key to listOf(preview("tt1"))),
                )
            val viewModel = SearchViewModel(FakeAccountRepository(), catalogs)
            advanceUntilIdle()

            viewModel.onQueryChange("dune")
            advanceUntilIdle()
            assertEquals(1, viewModel.state.value.results.size)

            viewModel.onQueryChange("")
            advanceUntilIdle()
            assertTrue(
                viewModel.state.value.results
                    .isEmpty(),
            )
            assertFalse(viewModel.state.value.hasSearched)
        }
}
