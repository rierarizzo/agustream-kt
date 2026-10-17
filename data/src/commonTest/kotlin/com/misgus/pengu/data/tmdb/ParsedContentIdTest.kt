package com.misgus.pengu.data.tmdb

import kotlin.test.Test
import kotlin.test.assertEquals

class ParsedContentIdTest {
    @Test
    fun parsesAnImdbIdAndDropsAnEpisodeSuffix() {
        assertEquals(ContentIdKind.IMDB, parseContentId("tt0133093").kind)
        assertEquals("tt0133093", parseContentId("tt0133093").value)
        assertEquals("tt0133093", parseContentId("tt0133093:1:2").value)
        assertEquals("tt0133093", parseContentId("imdb:0133093").value)
    }

    @Test
    fun parsesTheExplicitProviderPrefixes() {
        assertEquals(ContentIdKind.THEMOVIEDB, parseContentId("tmdb:603").kind)
        assertEquals("603", parseContentId("tmdb:603").value)
        assertEquals(ContentIdKind.TVDB, parseContentId("tvdb:81189").kind)
        assertEquals("81189", parseContentId("tvdb:81189").value)
    }

    @Test
    fun leavesBareNumbersAndOtherAddonsToTheTitleSearch() {
        assertEquals(ContentIdKind.OTHER, parseContentId("12345").kind)
        assertEquals(ContentIdKind.OTHER, parseContentId("kitsu:1234").kind)
    }
}
