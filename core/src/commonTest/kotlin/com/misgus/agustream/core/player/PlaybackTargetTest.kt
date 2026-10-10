package com.misgus.agustream.core.player

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PlaybackTargetTest {
    @Test
    fun buildsMovieTarget() {
        val target = PlaybackTarget.fromContent(
            type = "movie",
            id = "tt1254207",
            title = "Big Buck Bunny",
            source = "https://example/movie.mp4",
        )
        assertEquals("tt1254207", target.contentId)
        assertEquals("tt1254207", target.progressKey)
        assertNull(target.videoId)
        assertNull(target.season)
        assertEquals(true, target.canReportProgress)
    }

    @Test
    fun buildsEpisodeTarget() {
        val target = PlaybackTarget.fromContent(
            type = "series",
            id = "tt0149460:2:16",
            title = "The Office",
            source = "https://example/ep.mp4",
        )
        assertEquals("tt0149460", target.contentId)
        assertEquals("tt0149460:2:16", target.videoId)
        assertEquals(2, target.season)
        assertEquals(16, target.episode)
        assertEquals("tt0149460_s2e16", target.progressKey)
    }

    @Test
    fun keepsContentIdsWithColons() {
        val target = PlaybackTarget.fromContent(
            type = "series",
            id = "tmdb:123:1:2",
            title = "Show",
            source = "https://example/ep.mp4",
        )
        assertEquals("tmdb:123", target.contentId)
        assertEquals(1, target.season)
        assertEquals(2, target.episode)
        assertEquals("tmdb:123_s1e2", target.progressKey)
    }

    @Test
    fun rawTargetCannotReportProgress() {
        val target = PlaybackTarget.raw("https://example/video.mp4")
        assertEquals(false, target.canReportProgress)
        assertNull(target.progressKey)
    }
}
