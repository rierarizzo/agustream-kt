package com.misgus.pengu.ui.home

import com.misgus.pengu.core.model.ContentTypes
import com.misgus.pengu.core.model.MetaPreview
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class HeroArtworkTest {
    private fun preview(
        background: String? = "https://addon/backdrop.jpg",
        poster: String? = "https://addon/poster.jpg",
        textless: String? = null,
    ) = MetaPreview(
        id = "tt1",
        type = ContentTypes.MOVIE,
        name = "Movie",
        poster = poster,
        background = background,
        textlessPoster = textless,
    )

    @Test
    fun aWideWindowKeepsTheBackdrop() {
        val art = heroArtwork(preview(textless = "https://tmdb/textless.jpg"), wide = true)

        assertEquals("https://addon/backdrop.jpg", art?.url)
        assertEquals(false, art?.fit)
    }

    @Test
    fun aPhonePrefersTheFittedTextlessPoster() {
        val art = heroArtwork(preview(textless = "https://tmdb/textless.jpg"), wide = false)

        assertEquals("https://tmdb/textless.jpg", art?.url)
        assertEquals(true, art?.fit)
    }

    @Test
    fun aPhoneFallsBackToTheBackdropWithoutATextlessPoster() {
        val art = heroArtwork(preview(), wide = false)

        assertEquals("https://addon/backdrop.jpg", art?.url)
        assertEquals(false, art?.fit)
    }

    @Test
    fun fallsBackToThePosterWhenThereIsNoBackdrop() {
        val art = heroArtwork(preview(background = null), wide = false)

        assertEquals("https://addon/poster.jpg", art?.url)
    }

    @Test
    fun noArtworkMeansNothingToDraw() {
        assertNull(heroArtwork(preview(background = null, poster = null), wide = false))
    }
}
