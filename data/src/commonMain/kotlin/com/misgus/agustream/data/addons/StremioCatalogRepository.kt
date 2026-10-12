package com.misgus.agustream.data.addons

import com.misgus.agustream.core.model.Addon
import com.misgus.agustream.core.model.MetaDetail
import com.misgus.agustream.core.model.MetaPreview
import com.misgus.agustream.core.repository.AddonRepository
import com.misgus.agustream.core.repository.CatalogRef
import com.misgus.agustream.core.repository.CatalogRepository
import com.misgus.agustream.core.util.runCatchingCancellable
import io.ktor.client.HttpClient
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * [CatalogRepository] backed by Stremio addons.
 *
 * Catalogs are read from the enabled addons' manifests, so any addon that
 * declares catalogs shows up without a hardcoded provider.
 */
class StremioCatalogRepository(
    private val addons: AddonRepository,
    private val client: HttpClient,
) : CatalogRepository {
    private val clients = mutableMapOf<String, StremioAddonClient>()

    override suspend fun catalogs(): List<CatalogRef> {
        val installed: List<Addon> = runCatchingCancellable { addons.all() }.getOrDefault(emptyList())
        // Resolve the clients up front (single-threaded) so the manifest fetches
        // can run in parallel: one addon no longer waits for the previous one's
        // round-trip.
        val targets =
            installed.mapNotNull { addon ->
                val baseUrl = normalizeAddonBaseUrl(addon.url)
                if (baseUrl.isEmpty()) null else addon to clientFor(baseUrl)
            }
        return coroutineScope {
            targets
                .map { (addon, addonClient) ->
                    async {
                        runCatchingCancellable {
                            val manifest = addonClient.fetchManifest()
                            manifest.catalogs
                                .filter { it.type == "movie" || it.type == "series" }
                                .map { catalog ->
                                    CatalogRef(
                                        addonName = addon.name ?: manifest.name,
                                        addonBaseUrl = addonClient.baseUrl,
                                        type = catalog.type,
                                        id = catalog.id,
                                        name = catalog.name,
                                        extras = catalog.extra,
                                    )
                                }
                        }.getOrDefault(emptyList())
                    }
                }.awaitAll()
                .flatten()
        }
    }

    override suspend fun items(
        ref: CatalogRef,
        extra: Map<String, String>?,
    ): List<MetaPreview> = clientFor(ref.addonBaseUrl).fetchCatalog(ref.type, ref.id, extra)

    override suspend fun detailedItems(
        ref: CatalogRef,
        extra: Map<String, String>?,
    ): List<MetaDetail> = clientFor(ref.addonBaseUrl).fetchCatalogDetailed(ref.type, ref.id, extra)

    private fun clientFor(baseUrl: String): StremioAddonClient = clients.getOrPut(baseUrl) { StremioAddonClient(baseUrl, client) }
}
