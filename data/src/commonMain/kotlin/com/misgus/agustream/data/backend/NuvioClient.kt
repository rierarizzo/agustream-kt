package com.misgus.agustream.data.backend

import com.misgus.agustream.core.BackendException
import com.misgus.agustream.core.util.runCatchingCancellable
import com.misgus.agustream.data.appJson
import com.misgus.agustream.data.arrOrNull
import com.misgus.agustream.data.objOrNull
import com.misgus.agustream.data.store.NoopSessionStore
import com.misgus.agustream.data.store.SessionStore
import com.misgus.agustream.data.stringOrNull
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readRawBytes
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * HTTP transport for a Nuvio backend.
 *
 * A Nuvio backend is a Supabase deployment, so this talks to it over plain
 * HTTP instead of pulling in the Supabase SDK:
 *
 * * discovery — `GET  <baseUrl>/.well-known/nuvio`
 * * auth      — `POST <baseUrl>/auth/v1/token?grant_type=password`
 * * refresh   — `POST <baseUrl>/auth/v1/token?grant_type=refresh_token`
 * * data      — `GET  <baseUrl>/rest/v1/<table>` (PostgREST)
 *
 * It owns the transport state (the discovery document and the session) and is
 * shared by the repositories, which are split by capability. Row-level security
 * on the backend scopes every read to the signed-in user.
 *
 * The session is renewed both when it is already expired before a request and
 * once when a request is answered with `401`, so a long-running app does not
 * keep sending a stale token.
 */
class NuvioClient(
    baseUrl: String = DEFAULT_BASE_URL,
    private val http: HttpClient,
    private val store: SessionStore = NoopSessionStore(),
) {
    private val baseUrl: String = baseUrl.trimEnd('/')

    private var connection: BackendConnection? = null
    var session: BackendSession? = null
        private set

    /** Serializes renewals so parallel requests do not refresh the token twice. */
    private val refreshMutex = Mutex()

    /** Fetches `<baseUrl>/.well-known/nuvio` and remembers the result. */
    suspend fun discover(): BackendConnection {
        val json = request("GET", "$baseUrl/.well-known/nuvio")
        val obj = json.objOrNull() ?: throw BackendException("Unexpected discovery response")
        return BackendConnection.fromJson(obj).also { connection = it }
    }

    /** Signs in with email + password and remembers (and persists) the session. */
    suspend fun signIn(
        email: String,
        password: String,
    ): BackendSession {
        ensureDiscovered()
        val body =
            JsonObject(
                mapOf(
                    "email" to JsonPrimitive(email),
                    "password" to JsonPrimitive(password),
                ),
            )
        val json = request("POST", "$baseUrl/auth/v1/token?grant_type=password", body = body)
        val obj = json.objOrNull() ?: throw BackendException("Unexpected sign-in response")
        val parsed = BackendSession.fromJson(obj)
        session = parsed
        store.write(parsed.toJson())
        return parsed
    }

    /**
     * Restores a persisted session, renewing it when it has expired.
     *
     * A missing, corrupt or unrenewable session is cleared and reported as
     * `false`. Cancellation is never converted into a `false` result.
     */
    suspend fun restoreSession(): Boolean {
        val stored = store.read() ?: return false
        val parsed =
            runCatchingCancellable { BackendSession.fromJson(stored) }.getOrElse {
                store.clear()
                return false
            }
        if (parsed.accessToken.isEmpty() || parsed.refreshToken.isEmpty()) {
            store.clear()
            return false
        }

        var current = parsed
        if (current.isExpired) {
            val renewed =
                runCatchingCancellable {
                    ensureDiscovered()
                    refreshSession(current)
                }.getOrNull()
            if (renewed == null) {
                store.clear()
                return false
            }
            current = renewed
        }

        session = current
        store.write(current.toJson())
        return true
    }

    /** Drops the session (memory + store), invalidating it on the backend. */
    suspend fun signOut() {
        val current = session
        session = null
        store.clear()
        if (current == null) return
        runCatchingCancellable {
            http.post("$baseUrl/auth/v1/logout") {
                header("apikey", connection?.publishableKey ?: "")
                header("Authorization", "Bearer ${current.accessToken}")
            }
        }
    }

    /**
     * Runs a PostgREST read on [table].
     *
     * [order] and [filter] are passed through as-is, e.g. `added_at.desc` and
     * `profile_id=eq.1`. Callers must escape user-supplied values in [filter]
     * (see `postgrestLiteral`).
     */
    suspend fun select(
        table: String,
        order: String? = null,
        filter: String? = null,
    ): List<JsonObject> {
        ensureDiscovered()
        val token = accessToken()
        val query =
            buildString {
                append("?select=*")
                order?.let { append("&order=$it") }
                filter?.let { append("&$it") }
            }
        val json = request("GET", "$baseUrl/rest/v1/$table$query", token = token)
        val array = json.arrOrNull() ?: throw BackendException("Expected a JSON array from $table")
        return array.mapNotNull { it.objOrNull() }
    }

    /** Inserts [row] into [table] (PostgREST `POST`, `return=minimal`). */
    suspend fun insert(
        table: String,
        row: JsonObject,
    ) {
        ensureDiscovered()
        val token = accessToken()
        request(
            "POST",
            "$baseUrl/rest/v1/$table",
            body = row,
            token = token,
            extraHeaders = mapOf("Prefer" to "return=minimal"),
        )
    }

    /** Calls a PostgREST RPC (`POST /rest/v1/rpc/<name>`) and ignores the result. */
    suspend fun callRpc(
        name: String,
        params: JsonObject,
    ) {
        ensureDiscovered()
        val token = accessToken()
        request(
            "POST",
            "$baseUrl/rest/v1/rpc/$name",
            body = params,
            token = token,
            extraHeaders = mapOf("Prefer" to "return=minimal"),
        )
    }

    /** Deletes the rows of [table] matching the PostgREST [filter]. */
    suspend fun delete(
        table: String,
        filter: String,
    ) {
        ensureDiscovered()
        val token = accessToken()
        request(
            "DELETE",
            "$baseUrl/rest/v1/$table?$filter",
            token = token,
            extraHeaders = mapOf("Prefer" to "return=minimal"),
        )
    }

    fun close() {
        http.close()
    }

    /** Current access token, renewing the session first when it has expired. */
    private suspend fun accessToken(): String {
        ensureFreshSession()
        return requireSession().accessToken
    }

    private fun requireSession(): BackendSession = session ?: throw BackendException("Not signed in")

    private suspend fun ensureDiscovered() {
        if (connection == null) discover()
    }

    /** Renews [session] in the background when its access token has expired. */
    private suspend fun ensureFreshSession() {
        val current = session ?: return
        if (!current.isExpired) return
        refreshCurrentSession(force = false)
    }

    /**
     * Exchanges the current refresh token for a new session.
     *
     * [force] renews even when the access token is not known to be expired,
     * which is what a `401` response triggers. A failed renewal drops the
     * session so the next call reports "Not signed in" instead of looping.
     */
    private suspend fun refreshCurrentSession(force: Boolean) {
        refreshMutex.withLock {
            val current = session ?: return
            if (!force && !current.isExpired) return
            ensureDiscovered()
            val refreshed =
                refreshSession(current) ?: run {
                    session = null
                    store.clear()
                    return
                }
            session = refreshed
            store.write(refreshed.toJson())
        }
    }

    /** Exchanges [current]'s refresh token for a new session. */
    private suspend fun refreshSession(current: BackendSession): BackendSession? =
        try {
            val body =
                JsonObject(
                    mapOf("refresh_token" to JsonPrimitive(current.refreshToken)),
                )
            val json = request("POST", "$baseUrl/auth/v1/token?grant_type=refresh_token", body = body)
            val refreshed = json.objOrNull()?.let(BackendSession::fromJson)
            when {
                refreshed == null || refreshed.accessToken.isEmpty() -> null
                refreshed.userId.isNotEmpty() -> refreshed
                else ->
                    refreshed.copy(
                        refreshToken = refreshed.refreshToken.ifEmpty { current.refreshToken },
                        userId = current.userId,
                        email = refreshed.email ?: current.email,
                    )
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            null
        }

    private suspend fun request(
        method: String,
        url: String,
        body: JsonElement? = null,
        token: String? = null,
        extraHeaders: Map<String, String> = emptyMap(),
        allowRetry: Boolean = true,
    ): JsonElement? {
        val response = performRequest(method, url, body, token, extraHeaders)

        // A token can be rejected while still looking valid (clock skew, revoked
        // session). Renew once and retry with the new token.
        if (response.status.value == 401 && token != null && allowRetry) {
            val renewed =
                runCatchingCancellable {
                    refreshCurrentSession(force = true)
                    session?.accessToken
                }.getOrNull()
            if (!renewed.isNullOrEmpty() && renewed != token) {
                return request(method, url, body, renewed, extraHeaders, allowRetry = false)
            }
        }

        if (response.status.value !in 200..299) {
            throw BackendException(
                errorMessage(response.readRawBytes().decodeToString())
                    ?: "Request to $url failed with HTTP ${response.status.value}",
                statusCode = response.status.value,
            )
        }

        val bytes = response.readRawBytes()
        if (bytes.isEmpty()) return null
        val text = bytes.decodeToString()
        return try {
            appJson.parseToJsonElement(text)
        } catch (error: Exception) {
            throw BackendException("Invalid JSON from $url", cause = error)
        }
    }

    /** Runs one HTTP call, translating transport failures but not cancellation. */
    private suspend fun performRequest(
        method: String,
        url: String,
        body: JsonElement?,
        token: String?,
        extraHeaders: Map<String, String>,
    ): HttpResponse {
        val apiKey = connection?.publishableKey
        return try {
            when (method) {
                "GET" ->
                    http.get(url) {
                        header("Accept", "application/json")
                        if (!apiKey.isNullOrEmpty()) header("apikey", apiKey)
                        token?.let { header("Authorization", "Bearer $it") }
                        extraHeaders.forEach { (k, v) -> header(k, v) }
                    }
                "POST" ->
                    http.post(url) {
                        header("Accept", "application/json")
                        if (!apiKey.isNullOrEmpty()) header("apikey", apiKey)
                        token?.let { header("Authorization", "Bearer $it") }
                        extraHeaders.forEach { (k, v) -> header(k, v) }
                        if (body != null) {
                            contentType(ContentType.Application.Json)
                            setBody(body.toString())
                        }
                    }
                "DELETE" ->
                    http.delete(url) {
                        header("Accept", "application/json")
                        if (!apiKey.isNullOrEmpty()) header("apikey", apiKey)
                        token?.let { header("Authorization", "Bearer $it") }
                        extraHeaders.forEach { (k, v) -> header(k, v) }
                    }
                else -> throw IllegalArgumentException("Unsupported method: $method")
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Exception) {
            throw BackendException("Request to $url failed", cause = error)
        }
    }

    /** Extracts a human-readable message from a Supabase error body. */
    private fun errorMessage(text: String): String? =
        runCatching {
            val decoded = appJson.parseToJsonElement(text).objOrNull()
            decoded?.let { obj ->
                listOf("msg", "error_description", "message", "error")
                    .firstNotNullOfOrNull { obj[it].stringOrNull()?.takeIf { value -> value.isNotEmpty() } }
            }
        }.getOrNull()

    companion object {
        /** Official Nuvio-hosted backend. */
        const val DEFAULT_BASE_URL = "https://api.nuvio.tv"
    }
}
