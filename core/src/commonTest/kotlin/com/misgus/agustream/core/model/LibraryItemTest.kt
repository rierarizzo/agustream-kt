package com.misgus.agustream.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LibraryItemTest {
    private fun item(releaseInfo: String?) =
        LibraryItem(
            id = "1",
            contentId = "tt1",
            contentType = "movie",
            name = "Movie",
            releaseInfo = releaseInfo,
        )

    @Test
    fun parsesSingleYear() {
        assertEquals(2019, item("2019").year)
    }

    @Test
    fun parsesFirstYearOfARange() {
        assertEquals(2019, item("2019-2023").year)
    }

    @Test
    fun returnsNullWhenThereIsNoYear() {
        assertNull(item(null).year)
        assertNull(item("TBA").year)
    }

    @Test
    fun buildsFromPreview() {
        val preview =
            MetaPreview(
                id = "tt2",
                type = "series",
                name = "Show",
                poster = "p",
                genres = listOf("Drama"),
            )
        val built = LibraryItem.fromPreview(preview)
        assertEquals("tt2", built.contentId)
        assertEquals("series", built.contentType)
        assertEquals("p", built.poster)
    }
}
