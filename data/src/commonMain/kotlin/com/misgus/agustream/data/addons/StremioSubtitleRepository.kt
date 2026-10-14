package com.misgus.agustream.data.addons

import com.misgus.agustream.core.model.Addon
import com.misgus.agustream.core.model.Subtitle
import com.misgus.agustream.core.repository.AddonRepository
import com.misgus.agustream.core.repository.SubtitleRepository
import com.misgus.agustream.core.util.runCatchingCancellable
import io.ktor.client.HttpClient
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * [SubtitleRepository] backed by Stremio addons.
 *
 * Subtitles are aggregated from every enabled addon, in parallel, and deduped
 * by URL. An addon that fails or has no subtitles for the title is skipped.
 */
class StremioSubtitleRepository(
    private val addons: AddonRepository,
    private val client: HttpClient,
) : SubtitleRepository {
    private val clients = AddonClientPool(client)

    override suspend fun all(
        type: String,
        id: String,
    ): List<Subtitle> {
        val installed: List<Addon> = runCatchingCancellable { addons.all() }.getOrDefault(emptyList())
        if (installed.isEmpty()) return emptyList()
        val lists =
            coroutineScope {
                installed.map { addon -> async { subtitlesFor(addon, type, id) } }.awaitAll()
            }
        val seen = mutableSetOf<String>()
        val result = mutableListOf<Subtitle>()
        for (list in lists) {
            for (subtitle in list) {
                if (subtitle.url.isEmpty() || !seen.add(subtitle.url)) continue
                result += subtitle
            }
        }
        return result
    }

    private suspend fun subtitlesFor(
        addon: Addon,
        type: String,
        id: String,
    ): List<Subtitle> {
        val baseUrl = normalizeAddonBaseUrl(addon.url)
        if (baseUrl.isEmpty()) return emptyList()
        return try {
            val name = addon.name ?: hostOf(baseUrl)
            clients.clientFor(baseUrl).fetchSubtitles(type, id).map { it.withAddon(name) }
        } catch (cancellation: kotlinx.coroutines.CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            emptyList()
        }
    }
}
