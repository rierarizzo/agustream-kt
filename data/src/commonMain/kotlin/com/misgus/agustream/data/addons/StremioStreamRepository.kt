package com.misgus.agustream.data.addons

import com.misgus.agustream.core.model.Addon
import com.misgus.agustream.core.repository.AddonRepository
import com.misgus.agustream.core.repository.StreamGroup
import com.misgus.agustream.core.repository.StreamRepository
import io.ktor.client.HttpClient
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * [StreamRepository] backed by Stremio addons.
 *
 * Unlike metadata, streams are **aggregated**: every enabled addon is asked, in
 * parallel, and the results are grouped by addon. An addon that fails or has no
 * streams for the title is skipped.
 */
class StremioStreamRepository(
    private val addons: AddonRepository,
    private val client: HttpClient,
) : StreamRepository {
    private val clients = mutableMapOf<String, StremioAddonClient>()

    override suspend fun all(type: String, id: String): List<StreamGroup> {
        val installed: List<Addon> = runCatching { addons.all() }.getOrDefault(emptyList())
        if (installed.isEmpty()) return emptyList()
        val groups = coroutineScope {
            installed.map { addon -> async { streamsFor(addon, type, id) } }.awaitAll()
        }
        return groups.filterNotNull()
    }

    private suspend fun streamsFor(addon: Addon, type: String, id: String): StreamGroup? {
        val baseUrl = normalizeAddonBaseUrl(addon.url)
        if (baseUrl.isEmpty()) return null
        return try {
            val streams = clientFor(baseUrl).fetchStreams(type, id)
            if (streams.isEmpty()) null
            else StreamGroup(addonName = addon.name ?: hostOf(baseUrl), streams = streams)
        } catch (_: Exception) {
            // A streams-less or unreachable addon is skipped.
            null
        }
    }

    private fun clientFor(baseUrl: String): StremioAddonClient =
        clients.getOrPut(baseUrl) { StremioAddonClient(baseUrl, client) }
}

internal fun hostOf(url: String): String =
    url.substringAfter("://", url).substringBefore('/').substringBefore(':')
