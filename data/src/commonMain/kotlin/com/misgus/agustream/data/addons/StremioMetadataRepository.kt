package com.misgus.agustream.data.addons

import com.misgus.agustream.core.model.AddonCatalog
import com.misgus.agustream.core.model.MetaDetail
import com.misgus.agustream.core.model.MetaPreview
import com.misgus.agustream.core.repository.AddonRepository
import com.misgus.agustream.core.repository.MetadataRepository
import io.ktor.client.HttpClient

/**
 * [MetadataRepository] backed by Stremio addons.
 *
 * Metadata is always resolved from the account's enabled addons, in their
 * order, and the first one that returns a meta wins. No provider is baked in.
 */
class StremioMetadataRepository(
    private val addons: AddonRepository,
    private val client: HttpClient,
) : MetadataRepository {
    private val clients = mutableMapOf<String, StremioAddonClient>()

    override suspend fun detail(type: String, id: String): MetaDetail? {
        for (baseUrl in candidates()) {
            val meta = runCatching { clientFor(baseUrl).fetchMeta(type, id) }.getOrNull()
            if (meta != null) return meta
        }
        return null
    }

    override suspend fun similar(type: String, id: String, genre: String?): List<MetaPreview> {
        for (baseUrl in candidates()) {
            val items = tryCatalog(baseUrl, type, id, genre)
            if (items != null) return items
        }
        return emptyList()
    }

    /** Enabled addon base URLs, in the account's order, without duplicates. */
    private suspend fun candidates(): List<String> {
        val urls = mutableListOf<String>()
        val installed = runCatching { addons.all() }.getOrNull() ?: return urls
        for (addon in installed) {
            val normalized = normalizeAddonBaseUrl(addon.url)
            if (normalized.isNotEmpty() && normalized !in urls) urls += normalized
        }
        return urls
    }

    /** Returns the similar titles, or `null` when this addon cannot provide them. */
    private suspend fun tryCatalog(
        baseUrl: String,
        type: String,
        id: String,
        genre: String?,
    ): List<MetaPreview>? {
        val addonClient = clientFor(baseUrl)
        return try {
            val manifest = addonClient.fetchManifest()
            val catalog = pickCatalog(manifest.catalogs, type) ?: return null

            val supportsGenre = catalog.extra.any { it.name == "genre" }
            val hasGenre = !genre.isNullOrEmpty()
            if (hasGenre && !supportsGenre) return null

            val extra = if (hasGenre) mapOf("genre" to genre!!) else null
            val items = addonClient.fetchCatalog(type, catalog.id, extra)
                .filter { it.id != id }
            items.ifEmpty { null }
        } catch (_: Exception) {
            null
        }
    }

    private fun clientFor(baseUrl: String): StremioAddonClient =
        clients.getOrPut(baseUrl) { StremioAddonClient(baseUrl, client) }

    /** First catalog of [type], which is good enough for a "similar" row. */
    private fun pickCatalog(catalogs: List<AddonCatalog>, type: String): AddonCatalog? =
        catalogs.firstOrNull { it.type == type }
}
