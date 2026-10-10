package com.misgus.agustream.core.watched

import com.misgus.agustream.core.model.MetaDetail
import com.misgus.agustream.core.model.MetaVideo
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Instant

class WatchedRuleTest {
    private val today = Instant.parse("2024-01-10T00:00:00Z")

    private fun series(vararg videos: MetaVideo): MetaDetail = MetaDetail(
        id = "tt1",
        type = "series",
        name = "Show",
        videos = videos.toList(),
    )

    @Test
    fun notWatchedWhenAnAiredEpisodeIsMissing() {
        val meta = series(
            MetaVideo(id = "tt1:1:1", season = 1, episode = 1, released = "2024-01-01"),
            MetaVideo(id = "tt1:1:2", season = 1, episode = 2, released = "2024-01-01"),
        )
        val watched = setOf(watchedKey("tt1", 1, 1))
        assertFalse(hasWatchedAllMainSeasonEpisodes(meta, watched, emptySet(), today))
    }

    @Test
    fun watchedWhenEveryAiredEpisodeIsWatchedOrCompleted() {
        val meta = series(
            MetaVideo(id = "tt1:1:1", season = 1, episode = 1, released = "2024-01-01"),
            MetaVideo(id = "tt1:1:2", season = 1, episode = 2, released = "2024-01-01"),
        )
        val watched = setOf(watchedKey("tt1", 1, 1))
        val completed = setOf(watchedKey("tt1", 1, 2))
        assertTrue(hasWatchedAllMainSeasonEpisodes(meta, watched, completed, today))
    }

    @Test
    fun ignoresFutureEpisodesAndSpecials() {
        val meta = series(
            MetaVideo(id = "tt1:1:1", season = 1, episode = 1, released = "2024-01-01"),
            MetaVideo(id = "tt1:1:2", season = 1, episode = 2, released = "2030-01-01"),
            MetaVideo(id = "tt1:0:1", season = 0, episode = 1, released = "2024-01-01"),
        )
        val watched = setOf(watchedKey("tt1", 1, 1))
        assertTrue(hasWatchedAllMainSeasonEpisodes(meta, watched, emptySet(), today))
    }

    @Test
    fun seriesWithNoAiredEpisodesIsNotWatched() {
        val meta = series(
            MetaVideo(id = "tt1:1:1", season = 1, episode = 1, released = "2030-01-01"),
        )
        assertFalse(hasWatchedAllMainSeasonEpisodes(meta, emptySet(), emptySet(), today))
    }

    @Test
    fun unknownReleaseDateCountsAsAired() {
        assertTrue(isEpisodeAired(null, today))
        assertTrue(isEpisodeAired("not-a-date", today))
        assertFalse(isEpisodeAired("2030-01-01", today))
    }
}
