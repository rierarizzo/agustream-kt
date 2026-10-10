package com.misgus.agustream.ui.services

import androidx.compose.runtime.staticCompositionLocalOf
import com.misgus.agustream.core.player.PlayerService
import com.misgus.agustream.core.repository.AccountRepository
import com.misgus.agustream.core.repository.AddonRepository
import com.misgus.agustream.core.repository.CatalogRepository
import com.misgus.agustream.core.repository.LibraryRepository
import com.misgus.agustream.core.repository.MetadataRepository
import com.misgus.agustream.core.repository.NoRatingRepository
import com.misgus.agustream.core.repository.NoSubtitleRepository
import com.misgus.agustream.core.repository.ProgressRepository
import com.misgus.agustream.core.repository.RatingRepository
import com.misgus.agustream.core.repository.StreamRepository
import com.misgus.agustream.core.repository.SubtitleRepository
import com.misgus.agustream.core.watched.NoopWatchedSeriesCache
import com.misgus.agustream.core.watched.WatchedBadgeResolver
import com.misgus.agustream.core.watched.WatchedSeriesCache
import com.misgus.agustream.ui.shell.AppChrome
import com.misgus.agustream.ui.window.WindowController

/**
 * App-wide repositories and platform services, handed down the composition.
 *
 * Screens read data from here instead of reaching into `data/`, which keeps the
 * `ui -> domain` direction and lets tests inject in-memory implementations.
 *
 * It also owns the shared [watchedBadges] resolver, so the library and Home
 * reuse the same in-memory and on-disk cache.
 */
class AppServices(
    /** Session and profiles. */
    val account: AccountRepository,
    /** Saved titles. */
    val library: LibraryRepository,
    /** Watch progress. */
    val progress: ProgressRepository,
    /** Title metadata from Stremio addons. */
    val metadata: MetadataRepository,
    /** Playable sources from Stremio addons. */
    val streams: StreamRepository,
    /** Catalogs exposed by Stremio addons. */
    val catalogs: CatalogRepository,
    /** The playback engine. */
    val player: PlayerService,
    /** Native window control. */
    val window: WindowController,
    /** Subtitle tracks exposed by Stremio addons. */
    val subtitles: SubtitleRepository = NoSubtitleRepository(),
    /** Local per-title ratings (the backend has no server field for them). */
    val ratings: RatingRepository = NoRatingRepository(),
    /** Remembered per-series "fully watched" results. */
    val watchedCache: WatchedSeriesCache = NoopWatchedSeriesCache(),
    /** Immersive-mode state, shared with the player. */
    val chrome: AppChrome = AppChrome(),
    /** Addons installed in the account; used by the addon repository layer. */
    val addons: AddonRepository? = null,
) {
    /** Watched-badge resolver, shared by the library and Home. */
    val watchedBadges: WatchedBadgeResolver =
        WatchedBadgeResolver(progress = progress, metadata = metadata, cache = watchedCache)
}

val LocalAppServices = staticCompositionLocalOf<AppServices> {
    error("No AppServices provided in the composition")
}
