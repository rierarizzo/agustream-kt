package com.misgus.agustream.data.addons

import io.ktor.client.HttpClient
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Reuses one [StremioAddonClient] per addon base URL.
 *
 * A client caches the addon manifest, so sharing it across repositories (and
 * across concurrent catalog fetches) avoids refetching the same manifest. The
 * mutex makes the lazy creation safe when several catalogs are fetched in
 * parallel.
 */
internal class AddonClientPool(
    private val httpClient: HttpClient,
) {
    private val mutex = Mutex()
    private val clients = mutableMapOf<String, StremioAddonClient>()

    suspend fun clientFor(baseUrl: String): StremioAddonClient =
        mutex.withLock {
            clients.getOrPut(baseUrl) { StremioAddonClient(baseUrl, httpClient) }
        }
}
