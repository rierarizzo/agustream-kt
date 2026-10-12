package com.misgus.agustream.data.backend

import com.misgus.agustream.data.store.NoopSessionStore
import com.misgus.agustream.data.store.SessionStore
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/** The Nuvio pieces a test needs, wired the way the backend factory wires them. */
class NuvioTestStack(
    val auth: NuvioAuth,
    val rest: NuvioRest,
)

/** Builds a [NuvioTestStack] backed by [engine]. */
fun nuvioTestStack(
    engine: MockEngine,
    baseUrl: String = "https://x",
    store: SessionStore = NoopSessionStore(),
): NuvioTestStack {
    val transport = NuvioTransport(baseUrl = baseUrl, http = HttpClient(engine) { expectSuccess = false })
    val discovery = NuvioDiscovery(transport)
    val auth = NuvioAuth(transport, discovery, store)
    return NuvioTestStack(auth, NuvioRest(transport, discovery, auth))
}

/** JSON content type shared by the Nuvio tests. */
val nuvioJsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")

/** Discovery document the Nuvio tests answer with. */
const val NUVIO_DISCOVERY_JSON =
    """{"version":1,"service":"nuvio","backend_url":"https://x","publishable_key":"anon"}"""

/** Refresh response the Nuvio tests answer with. */
fun nuvioRefreshBody(): String =
    """{"access_token":"new","refresh_token":"refresh-new","user":{"id":"u1","email":"me@example.com"},"expires_at":9999999999}"""

/** A session with the given [accessToken], expiring at [expiresAt] (epoch seconds). */
fun nuvioSessionJson(
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

/** In-memory [SessionStore] for the Nuvio tests. */
class MemorySessionStore(
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

/** Expiry far enough ahead that a session is never considered expired. */
const val NUVIO_FAR_FUTURE = 9_999_999_999L
