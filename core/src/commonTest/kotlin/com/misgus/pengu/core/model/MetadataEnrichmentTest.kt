package com.misgus.pengu.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame

class MetadataEnrichmentTest {
    private val tmdb =
        TmdbTitle(
            tmdbId = 603,
            mediaType = TmdbMediaType.MOVIE,
            title = "The Matrix",
            poster = "https://img/poster.jpg",
            textlessPoster = "https://img/textless.jpg",
            backdrop = "https://img/backdrop.jpg",
            logo = "https://img/logo.png",
            overview = "A hacker learns the truth.",
            releaseDate = "1999-03-31",
            runtimeMinutes = 136,
            genres = listOf("Action"),
            countries = listOf("United States of America"),
            cast = listOf(MetaPerson("Keanu Reeves", "Neo", "https://img/neo.jpg")),
            imdbId = "tt0133093",
        )

    @Test
    fun aPreviewKeepsTheAddonArtworkAndFillsTheRest() {
        val preview =
            MetaPreview(
                id = "tt0133093",
                type = ContentTypes.MOVIE,
                name = "The Matrix",
                poster = "https://addon/poster.jpg",
            )

        val enriched = preview.enrichedWith(tmdb)

        assertEquals("tt0133093", enriched.id)
        assertEquals("https://addon/poster.jpg", enriched.poster)
        assertEquals("https://img/backdrop.jpg", enriched.background)
        assertEquals("https://img/textless.jpg", enriched.textlessPoster)
        assertEquals("https://img/logo.png", enriched.logo)
        assertEquals("1999", enriched.releaseInfo)
    }

    @Test
    fun aDetailNeverReplacesTheIdTheVideosOrAFilledField() {
        val video = MetaVideo(id = "tt0133093:1:1", title = "Pilot", season = Season(1), episode = Episode(1))
        val detail =
            MetaDetail(
                id = "tt0133093",
                type = ContentTypes.MOVIE,
                name = "The Matrix",
                description = "From the addon",
                videos = listOf(video),
            )

        val enriched = detail.enrichedWith(tmdb)

        assertNotNull(enriched)
        assertEquals("tt0133093", enriched.id)
        assertEquals(listOf(video), enriched.videos)
        assertEquals("From the addon", enriched.description)
        assertEquals("https://img/backdrop.jpg", enriched.background)
        assertEquals(136, enriched.runtimeMinutes)
        assertEquals(listOf(MetaPerson("Keanu Reeves", "Neo", "https://img/neo.jpg")), enriched.cast)
    }

    @Test
    fun aNullDetailBecomesAPageFromTmdbAlone() {
        val enriched = (null as MetaDetail?).enrichedWith(tmdb)

        assertNotNull(enriched)
        assertEquals("tmdb:603", enriched.id)
        assertEquals(ContentTypes.MOVIE, enriched.type)
        assertEquals("The Matrix", enriched.name)
        assertEquals("1999", enriched.releaseInfo)
        assertEquals("1999-03-31", enriched.released)
        assertEquals("United States of America", enriched.country)
    }

    @Test
    fun aNullTmdbLeavesEverythingUntouched() {
        val preview = MetaPreview(id = "tt1", type = ContentTypes.MOVIE, name = "Movie")

        assertSame(preview, preview.enrichedWith(null))
        assertNull((null as MetaDetail?).enrichedWith(null))
    }

    @Test
    fun aTmdbTitleBecomesAPrefixedPreview() {
        val preview = tmdb.toMetaPreview()

        assertEquals("tmdb:603", preview.id)
        assertEquals(ContentTypes.MOVIE, preview.type)
        assertEquals("The Matrix", preview.name)
        assertEquals("https://img/poster.jpg", preview.poster)
    }

    @Test
    fun aSeriesTmdbTitleMapsToTheSeriesType() {
        val series = tmdb.copy(mediaType = TmdbMediaType.SERIES)

        assertEquals(ContentTypes.SERIES, series.toMetaPreview().type)
    }
}
