package com.misgus.agustream.data.store

import com.misgus.agustream.core.watched.CachedSeriesState
import com.misgus.agustream.data.local.LocalLibraryRepository
import com.misgus.agustream.data.local.LocalProgressRepository
import com.misgus.agustream.core.model.LibraryItem
import com.misgus.agustream.core.model.WatchProgress
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.put
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class StoresTest {
    private fun tempDir(): File =
        File.createTempFile("agustream", "").let { file ->
            file.delete()
            file.mkdirs()
            file
        }

    @Test
    fun jsonFileStoreRoundTrips() = runTest {
        val path = File(tempDir(), "library.json").path
        val store = JsonFileStore(path)
        val row = kotlinx.serialization.json.JsonObject(
            mapOf("content_id" to kotlinx.serialization.json.JsonPrimitive("tt1")),
        )
        store.write(listOf(row))
        assertEquals(listOf(row), store.read())
    }

    @Test
    fun jsonFileStoreDegradesToEmptyOnCorruptFile() = runTest {
        val file = File(tempDir(), "broken.json")
        file.writeText("not json")
        assertTrue(JsonFileStore(file.path).read().isEmpty())
    }

    @Test
    fun jsonMapStoreRoundTrips() = runTest {
        val path = File(tempDir(), "ratings.json").path
        val store = JsonMapStore(path)
        store.write(
            kotlinx.serialization.json.JsonObject(
                mapOf("tt1" to kotlinx.serialization.json.JsonPrimitive(9)),
            ),
        )
        assertEquals("9", (store.read()["tt1"] as kotlinx.serialization.json.JsonPrimitive).content)
    }

    @Test
    fun watchedSeriesCacheStoresByProfile() = runTest {
        val path = File(tempDir(), "watched_series.json").path
        val cache = FileWatchedSeriesCache(path)
        assertNull(cache.read("profile-1", "tt1"))
        cache.writeAll("profile-1", mapOf("tt1" to CachedSeriesState("sig", true)))
        assertEquals(CachedSeriesState("sig", true), cache.read("profile-1", "tt1"))
        cache.clear("profile-1")
        assertNull(cache.read("profile-1", "tt1"))
    }

    @Test
    fun localLibraryRepositoryAddsAndRemoves() = runTest {
        val path = File(tempDir(), "library.json").path
        val repository = LocalLibraryRepository(JsonFileStore(path))
        val item = LibraryItem(id = "", contentId = "tt1", contentType = "movie", name = "Movie")
        repository.add(item)
        repository.add(item)
        assertEquals(1, repository.all().size)
        assertTrue(repository.contains("tt1"))
        repository.remove("tt1")
        assertFalse(repository.contains("tt1"))
    }

    @Test
    fun localProgressRepositorySavesAndMarksWatched() = runTest {
        val dir = tempDir()
        val repository = LocalProgressRepository(
            store = JsonFileStore(File(dir, "progress.json").path),
            watched = JsonFileStore(File(dir, "watched.json").path),
        )
        repository.save(
            WatchProgress(
                id = "",
                contentId = "tt1",
                contentType = "movie",
                position = 55.minutes,
                duration = 60.minutes,
                lastWatched = Instant.parse("2024-01-01T00:00:00Z"),
                progressKey = "tt1",
            ),
        )
        assertEquals(1, repository.all().size)
        // Completed progress is reported as watched.
        assertTrue(repository.watchedEntries(listOf("tt1")).isNotEmpty())

        repository.markWatched("tt2", "movie")
        assertTrue(repository.watchedEntries(listOf("tt2")).any { it.isMarker })
        repository.unmarkWatched("tt2")
        assertTrue(repository.watchedEntries(listOf("tt2")).isEmpty())
    }
}
