package com.misgus.agustream.data.backend

import com.misgus.agustream.data.instantMillisOrNull
import com.misgus.agustream.data.intOrNull
import com.misgus.agustream.data.longOrNull
import com.misgus.agustream.data.objOrNull
import com.misgus.agustream.data.stringOrNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * A signed-in session, backed by an access/refresh token pair.
 *
 * Transport state, not a domain model: only the backend client reads and writes
 * it.
 */
data class BackendSession(
    val accessToken: String,
    /** Long-lived token used to renew [accessToken]. */
    val refreshToken: String,
    val userId: String,
    val email: String? = null,
    /** Expiry of [accessToken], in UTC. */
    val expiresAt: Instant? = null,
) {
    val isExpired: Boolean
        get() = expiresAt != null && Clock.System.now() >= expiresAt

    /** Serializes the session for the [com.misgus.agustream.data.store.SessionStore]. */
    fun toJson(): JsonObject {
        val map =
            mutableMapOf<String, kotlinx.serialization.json.JsonElement>(
                "access_token" to JsonPrimitive(accessToken),
                "refresh_token" to JsonPrimitive(refreshToken),
                "user" to
                    JsonObject(
                        buildMap {
                            put("id", JsonPrimitive(userId))
                            email?.let { put("email", JsonPrimitive(it)) }
                        },
                    ),
            )
        // `expires_at` is stored in seconds, matching the auth API.
        expiresAt?.let { map["expires_at"] = JsonPrimitive(it.epochSeconds) }
        return JsonObject(map)
    }

    companion object {
        fun fromJson(json: JsonObject): BackendSession {
            val user = json["user"].objOrNull()
            // Seconds since the epoch do not fit in an Int for far-future values.
            val expiresAtSeconds = json["expires_at"].longOrNull()
            return BackendSession(
                accessToken = json["access_token"].stringOrNull() ?: "",
                refreshToken = json["refresh_token"].stringOrNull() ?: "",
                userId = user?.get("id").stringOrNull() ?: "",
                email = user?.get("email").stringOrNull(),
                expiresAt =
                    expiresAtSeconds?.let { Instant.fromEpochSeconds(it) }
                        ?: json["expires_at"].instantMillisOrNull(),
            )
        }
    }
}

/**
 * Connection settings published by the backend at
 * `<baseUrl>/.well-known/<service>`.
 *
 * Discovery/transport state, specific to the Nuvio backend (a Supabase
 * deployment).
 */
data class BackendConnection(
    val version: Int,
    val service: String,
    val backendUrl: String,
    /** Public client key (Supabase anon key). Not a secret. */
    val publishableKey: String,
    val selfHosted: Boolean = false,
    val capabilities: BackendCapabilities = BackendCapabilities(),
) {
    companion object {
        fun fromJson(json: JsonObject): BackendConnection {
            val capabilities = json["capabilities"].objOrNull()
            return BackendConnection(
                version = json["version"].intOrNull() ?: 0,
                service = json["service"].stringOrNull() ?: "",
                backendUrl = json["backend_url"].stringOrNull() ?: "",
                publishableKey = json["publishable_key"].stringOrNull() ?: "",
                selfHosted =
                    json["self_hosted"].let {
                        (it as? JsonPrimitive)?.content?.lowercase() == "true"
                    },
                capabilities =
                    BackendCapabilities(
                        emailPasswordAuth =
                            (capabilities?.get("email_password_auth") as? JsonPrimitive)?.content == "true",
                        tvLogin = (capabilities?.get("tv_login") as? JsonPrimitive)?.content == "true",
                    ),
            )
        }
    }
}

/** Features advertised by the backend. */
data class BackendCapabilities(
    /** Whether direct email + password sign-in is available. */
    val emailPasswordAuth: Boolean = false,
    /** Whether the device-code TV sign-in flow is available. */
    val tvLogin: Boolean = false,
)
