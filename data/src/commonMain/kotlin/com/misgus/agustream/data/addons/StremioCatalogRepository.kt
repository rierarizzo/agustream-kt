package com.misgus.agustream.data.addons

import com.misgus.agustream.core.model.Addon
import com.misgus.agustream.core.model.MetaPreview
import com.misgus.agustream.core.repository.AddonRepository
import com.misgus.agustream.core.repository.CatalogRef
import com.misgus.agustream.core.repository.CatalogRepository
import com.misgus.agustream.core.util.runCatchingCancellable
import io.ktor.client.HttpClient

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
        val refs = mutableListOf<CatalogRef>()
        for (addon in installed) {
            val baseUrl = normalizeAddonBaseUrl(addon.url)
            if (baseUrl.isEmpty()) continue
            runCatchingCancellable {
                val manifest = clientFor(baseUrl).fetchManifest()
                for (catalog in manifest.catalogs) {
                    if (catalog.type != "movie" && catalog.type != "series") continue
                    refs +=
                        CatalogRef(
                            addonName = addon.name ?: manifest.name,
                            addonBaseUrl = baseUrl,
                            type = catalog.type,
                            id = catalog.id,
                            name = catalog.name,
                        )
                }
            }
            // An addon with no manifest is skipped.
        }
        return refs
    }

    override suspend fun items(
        ref: CatalogRef,
        extra: Map<String, String>?,
    ): List<MetaPreview> = clientFor(ref.addonBaseUrl).fetchCatalog(ref.type, ref.id, extra)

    private fun clientFor(baseUrl: String): StremioAddonClient = clients.getOrPut(baseUrl) { StremioAddonClient(baseUrl, client) }
}
