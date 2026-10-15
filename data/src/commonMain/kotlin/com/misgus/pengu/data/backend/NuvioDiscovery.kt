package com.misgus.pengu.data.backend

import com.misgus.pengu.core.BackendException
import com.misgus.pengu.data.objOrNull

/**
 * Fetches and remembers the backend's discovery document
 * (`<baseUrl>/.well-known/nuvio`).
 *
 * The document carries the publishable key the other requests need, so it is
 * resolved once and reused through [publishableKey]. Discovery itself needs no
 * credentials.
 */
class NuvioDiscovery(
    private val transport: NuvioTransport,
) {
    /** Document of the discovered backend, or `null` before the first fetch. */
    var connection: BackendConnection? = null
        private set

    /** Publishable key of the discovered backend, or `null` when there is none. */
    val publishableKey: String?
        get() = connection?.publishableKey?.takeIf { it.isNotEmpty() }

    /** Fetches the document and remembers it. */
    suspend fun discover(): BackendConnection {
        val json = transport.request("GET", "${transport.baseUrl}/.well-known/nuvio")
        val obj = json.objOrNull() ?: throw BackendException("Unexpected discovery response")
        return BackendConnection.fromJson(obj).also { connection = it }
    }

    /** Discovers once; later calls are no-ops. */
    suspend fun ensure() {
        if (connection == null) discover()
    }
}
