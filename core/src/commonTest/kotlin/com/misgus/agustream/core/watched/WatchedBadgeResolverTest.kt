package com.misgus.agustream.core.watched

import com.misgus.agustream.core.model.MetaDetail
import com.misgus.agustream.core.model.MetaVideo
import com.misgus.agustream.core.model.WatchProgress
import com.misgus.agustream.core.model.WatchedEntry
import com.misgus.agustream.core.repository.MetadataRepository
import com.misgus.agustream.core.repository.ProgressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class WatchedBadgeResolverTest {
    private val today = Instant.parse("2024-01-10T00:00:00Z")

    @Test
    fun movieIsWatchedFromATitleMarker() = runTest {
        val progress = FakeProgress(
            entries = listOf(WatchedEntry("tt-movie", "movie")),
        )
        val resolver = WatchedBadgeResolver(progress, FakeMetadata())
        val watched = resolver.resolve(
            listOf(WatchedCandidate("tt-movie", "movie"), WatchedCandidate("tt-other", "movie")),
            today = today,
        )
        assertEquals(setOf("tt-movie"), watched)
    }

    @Test
    fun seriesIsWatchedWhenEveryAiredEpisodeIsCompleted() = runTest {
        val series = "tt-series"
        val meta = MetaDetail(
            id = series,
            type = "series",
            name = "Show",
            videos = listOf(
                MetaVideo(id = "$series:1:1", season = 1, episode = 1, released = "2024-01-01"),
                MetaVideo(id = "$series:1:2", season = 1, episode = 2, released = "2024-01-01"),
            ),
        )
        val progress = FakeProgress(
            entries = listOf(WatchedEntry(series, "series", season = 1, episode = 1)),
            all = listOf(
                WatchProgress(
                    id = "1",
                    contentId = series,
                    contentType = "series",
                    season = 1,
                    episode = 1,
                    position = 50.minutes,
                    duration = 50.minutes,
                    progressKey = "${series}_s1e1",
                ),
                WatchProgress(
                    id = "2",
                    contentId = series,
                    contentType = "series",
                    season = 1,
                    episode = 2,
                    position = 50.minutes,
                    duration = 50.minutes,
                    progressKey = "${series}_s1e2",
                ),
            ),
        )
        val resolver = WatchedBadgeResolver(progress, FakeMetadata(meta))
        val watched = resolver.resolve(listOf(WatchedCandidate(series, "series")), today = today)
        assertEquals(setOf(series), watched)
    }

    @Test
    fun seriesWithoutActivityIsNotLookedUp() = runTest {
        val progress = FakeProgress()
        val metadata = FakeMetadata()
        val resolver = WatchedBadgeResolver(progress, metadata)
        val watched = resolver.resolve(listOf(WatchedCandidate("tt-series", "series")), today = today)
        assertEquals(emptySet(), watched)
        assertEquals(0, metadata.detailCalls)
    }

    private class FakeProgress(
        private val entries: List<WatchedEntry> = emptyList(),
        private val all: List<WatchProgress> = emptyList(),
    ) : ProgressRepository {
        override val changes: StateFlow<Long> = MutableStateFlow(0)
        override suspend fun all(): List<WatchProgress> = all
        override suspend fun save(progress: WatchProgress) = Unit
        override suspend fun watchedEntries(candidateIds: Collection<String>): List<WatchedEntry> =
            entries.filter { it.contentId in candidateIds }

        override suspend fun markWatched(
            contentId: String,
            contentType: String,
            season: Int?,
            episode: Int?,
        ) = Unit

        override suspend fun unmarkWatched(contentId: String, season: Int?, episode: Int?) = Unit
    }

    private class FakeMetadata(private val meta: MetaDetail? = null) : MetadataRepository {
        var detailCalls = 0
        override suspend fun detail(type: String, id: String): MetaDetail? {
            detailCalls++
            return meta
        }

        override suspend fun similar(type: String, id: String, genre: String?) = emptyList<Nothing>()
    }
}
