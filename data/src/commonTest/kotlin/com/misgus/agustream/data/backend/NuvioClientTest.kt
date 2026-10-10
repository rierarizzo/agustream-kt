package com.misgus.agustream.data.backend

import com.misgus.agustream.data.store.SessionStore
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NuvioClientTest {
    private val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")

    @Test
    fun renewsSessionAfter401AndRetriesOnce() =
        runTest {
            var refreshCalls = 0
            var dataCalls = 0
            val engine =
                MockEngine { request ->
                    val path = request.url.encodedPath
                    val authorization = request.headers[HttpHeaders.Authorization]
                    when {
                        path.endsWith("/.well-known/nuvio") ->
                            respond(
                                """{"version":1,"service":"nuvio","backend_url":"https://x","publishable_key":"anon"}""",
                                HttpStatusCode.OK,
                                jsonHeaders,
                            )
                        path.endsWith("/auth/v1/token") &&
                            request.url.parameters["grant_type"] == "refresh_token" -> {
                            refreshCalls++
                            respond(refreshBody(), HttpStatusCode.OK, jsonHeaders)
                        }
                        path.contains("/rest/v1/library_items") -> {
                            dataCalls++
                            if (authorization == "Bearer old") {
                                respond("", HttpStatusCode.Unauthorized)
                            } else {
                                respond("[]", HttpStatusCode.OK, jsonHeaders)
                            }
                        }
                        else -> respond("{}", HttpStatusCode.NotFound, jsonHeaders)
                    }
                }
            val client = client(engine, store = MemorySessionStore(sessionJson("old", expiresAt = FAR_FUTURE)))
            assertTrue(client.restoreSession())

            val rows = client.select("library_items")

            assertEquals(0, rows.size)
            assertEquals(1, refreshCalls)
            assertEquals(2, dataCalls)
            assertEquals("new", client.session?.accessToken)
        }

    @Test
    fun renewsAnExpiredSessionWhenRestoring() =
        runTest {
            var refreshCalls = 0
            var dataCalls = 0
            val engine =
                MockEngine { request ->
                    val path = request.url.encodedPath
                    when {
                        path.endsWith("/.well-known/nuvio") ->
                            respond(
                                """{"version":1,"service":"nuvio","backend_url":"https://x","publishable_key":"anon"}""",
                                HttpStatusCode.OK,
                                jsonHeaders,
                            )
                        path.endsWith("/auth/v1/token") -> {
                            refreshCalls++
                            respond(refreshBody(), HttpStatusCode.OK, jsonHeaders)
                        }
                        path.contains("/rest/v1/library_items") -> {
                            dataCalls++
                            respond("[]", HttpStatusCode.OK, jsonHeaders)
                        }
                        else -> respond("{}", HttpStatusCode.NotFound, jsonHeaders)
                    }
                }
            val client = client(engine, store = MemorySessionStore(sessionJson("old", expiresAt = 0)))

            assertTrue(client.restoreSession())
            val rows = client.select("library_items")

            assertEquals(0, rows.size)
            assertEquals(1, refreshCalls)
            assertEquals(1, dataCalls)
            assertEquals("new", client.session?.accessToken)
        }

    private fun client(
        engine: MockEngine,
        store: SessionStore,
    ): NuvioClient =
        NuvioClient(
            baseUrl = "https://x",
            http = HttpClient(engine) { expectSuccess = false },
            store = store,
        )

    private fun sessionJson(
        accessToken: String,
        expiresAt: Long,
    ): JsonObject =
        buildJsonObject {
            put("access_token", accessToken)
            put("refresh_token", "refresh-old")
            put(
                "user",
                buildJsonObject {
                    put("id", "u1")
                    put("email", "me@example.com")
                },
            )
            put("expires_at", expiresAt)
        }

    private fun refreshBody(): String =
        """{"access_token":"new","refresh_token":"refresh-new","user":{"id":"u1","email":"me@example.com"},"expires_at":9999999999}"""

    private class MemorySessionStore(
        private var value: JsonObject? = null,
    ) : SessionStore {
        override suspend fun read(): JsonObject? = value

        override suspend fun write(session: JsonObject) {
            value = session
        }

        override suspend fun clear() {
            value = null
        }
    }

    private companion object {
        const val FAR_FUTURE = 9_999_999_999L
    }
}
