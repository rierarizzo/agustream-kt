package com.misgus.agustream.data.backend

import com.misgus.agustream.core.BackendException
import com.misgus.agustream.core.util.runCatchingCancellable
import com.misgus.agustream.data.objOrNull
import com.misgus.agustream.data.store.NoopSessionStore
import com.misgus.agustream.data.store.SessionStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * The signed-in session: sign-in, restore, refresh and sign-out.
 *
 * It owns the [BackendSession] and the [SessionStore], and serializes renewals so
 * parallel requests never refresh the same token twice. It does no data access;
 * [NuvioRest] asks it for the access token instead.
 */
class NuvioAuth(
    private val transport: NuvioTransport,
    private val discovery: NuvioDiscovery,
    private val store: SessionStore = NoopSessionStore(),
) {
    /** Current session, or `null` when signed out. */
    var session: BackendSession? = null
        private set

    /** Serializes renewals so parallel requests do not refresh the token twice. */
    private val refreshMutex = Mutex()

    /** Signs in with email + password and remembers (and persists) the session. */
    suspend fun signIn(
        email: String,
        password: String,
    ): BackendSession {
        discovery.ensure()
        val body =
            JsonObject(
                mapOf(
                    "email" to JsonPrimitive(email),
                    "password" to JsonPrimitive(password),
                ),
            )
        val json =
            transport.request(
                "POST",
                "${transport.baseUrl}/auth/v1/token?grant_type=password",
                body = body,
                apiKey = discovery.publishableKey,
            )
        val obj = json.objOrNull() ?: throw BackendException("Unexpected sign-in response")
        return BackendSession.fromJson(obj).also {
            session = it
            store.write(it.toJson())
        }
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
                    discovery.ensure()
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
            transport.send(
                "POST",
                "${transport.baseUrl}/auth/v1/logout",
                token = current.accessToken,
                apiKey = discovery.publishableKey,
            )
        }
    }

    /** Current access token, renewing the session first when it has expired. */
    suspend fun accessToken(): String {
        if (session?.isExpired == true) refresh(force = false)
        return (session ?: throw BackendException("Not signed in")).accessToken
    }

    /**
     * Exchanges the current refresh token for a new session.
     *
     * [force] renews even when the access token is not known to be expired, which
     * is what a `401` response triggers. A failed renewal drops the session so
     * the next call reports "Not signed in" instead of looping.
     */
    suspend fun refresh(force: Boolean) {
        refreshMutex.withLock {
            val current = session ?: return
            if (!force && !current.isExpired) return
            discovery.ensure()
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
            val json =
                transport.request(
                    "POST",
                    "${transport.baseUrl}/auth/v1/token?grant_type=refresh_token",
                    body = body,
                    apiKey = discovery.publishableKey,
                )
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
}
