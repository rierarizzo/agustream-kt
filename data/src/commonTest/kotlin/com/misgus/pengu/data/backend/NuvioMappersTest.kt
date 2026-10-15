package com.misgus.pengu.data.backend

import com.misgus.pengu.core.model.LibraryItem
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class NuvioMappersTest {
    @Test
    fun mapsLibraryRow() {
        val row =
            buildJsonObject {
                put("id", "row-1")
                put("content_id", "tt1")
                put("content_type", "movie")
                put("name", "Movie")
                put("imdb_rating", "7.9")
                put("genres", buildJsonArray { add(JsonPrimitive("Action")) })
                put("added_at", 1_700_000_000_000L)
                put("profile_id", 2)
            }
        val item = libraryItemFromRow(row)
        assertEquals("tt1", item.contentId)
        assertEquals(7.9, item.imdbRating)
        assertEquals(listOf("Action"), item.genres)
        assertEquals(1_700_000_000_000L, item.addedAt?.toEpochMilliseconds())
        assertEquals(2, item.profileId)
    }

    @Test
    fun mapsWatchProgressTimesInMilliseconds() {
        val row =
            buildJsonObject {
                put("id", "p1")
                put("content_id", "tt1")
                put("content_type", "movie")
                put("position", 167_000L)
                put("duration", 6_000_000L)
                put("last_watched", 1_700_000_000_000L)
            }
        val progress = watchProgressFromRow(row)
        assertEquals(167_000L, progress.position?.inWholeMilliseconds)
        assertEquals(6_000_000L, progress.duration?.inWholeMilliseconds)
        assertNotNull(progress.fraction)
    }

    @Test
    fun serializesLibraryRowWithoutNullColumns() {
        val item =
            LibraryItem(
                id = "",
                contentId = "tt1",
                contentType = "movie",
                name = "Movie",
            )
        val row = libraryItemToRow(item, profileId = 1, userId = "user-1")
        assertEquals("tt1", row["content_id"]?.let { (it as JsonPrimitive).content })
        assertEquals("user-1", row["user_id"]?.let { (it as JsonPrimitive).content })
        assertNull(row["poster"])
        assertNotNull(row["added_at"])
    }
}

class BackendSessionTest {
    @Test
    fun roundTripsThroughJson() {
        val session =
            BackendSession(
                accessToken = "access",
                refreshToken = "refresh",
                userId = "user-1",
                email = "me@example.com",
                expiresAt = kotlin.time.Instant.parse("2030-01-01T00:00:00Z"),
            )
        val restored = BackendSession.fromJson(session.toJson())
        assertEquals(session, restored)
    }

    @Test
    fun expiredSessionIsDetected() {
        val expired =
            BackendSession(
                accessToken = "a",
                refreshToken = "b",
                userId = "u",
                expiresAt = kotlin.time.Instant.parse("2000-01-01T00:00:00Z"),
            )
        assertEquals(true, expired.isExpired)
    }

    @Test
    fun missingExpiryMeansNotExpired() {
        val session = BackendSession(accessToken = "a", refreshToken = "b", userId = "u")
        assertEquals(false, session.isExpired)
    }
}
