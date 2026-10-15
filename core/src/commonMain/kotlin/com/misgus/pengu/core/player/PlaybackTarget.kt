package com.misgus.pengu.core.player

import com.misgus.pengu.core.model.ContentType
import com.misgus.pengu.core.model.Episode
import com.misgus.pengu.core.model.Season

/**
 * What the player is asked to play, plus the identity needed to save progress.
 *
 * [source] is a direct URL or a local path. The content fields mirror the
 * backend's `watch_progress` row, so the player can report progress without
 * knowing where the request came from.
 */
data class PlaybackTarget(
    val source: String,
    val title: String,
    val contentId: String? = null,
    /** `movie`, `series`, ... */
    val contentType: ContentType? = null,
    /** Playback id of the episode, when it is one. */
    val videoId: String? = null,
    val season: Season? = null,
    val episode: Episode? = null,
    /** Headers the player must send (an addon's `proxyHeaders`). */
    val httpHeaders: Map<String, String>? = null,
) {
    /** Whether progress can be saved for this target. */
    val canReportProgress: Boolean get() = contentId != null

    /**
     * Nuvio's stable progress identity: `content_s{season}e{episode}` for an
     * episode, the content id for a movie.
     */
    val progressKey: String?
        get() {
            val id = contentId ?: return null
            val s = season
            val e = episode
            return if (s != null && e != null) "${id}_s${s}e$e" else id
        }

    companion object {
        /**
         * Builds a target from a content request and the chosen stream.
         *
         * [id] is the playable id: a movie id (`tt123`), or an episode id
         * (`tt0149460:2:16`, possibly with a prefixed content id like
         * `tmdb:123:1:2`).
         */
        fun fromContent(
            type: ContentType,
            id: String,
            title: String,
            source: String,
            httpHeaders: Map<String, String>? = null,
        ): PlaybackTarget {
            // Split from the end so content ids with colons (`tmdb:123`) survive.
            val parts = id.split(':')
            var season: Season? = null
            var episode: Episode? = null
            if (parts.size >= 3) {
                season = parts[parts.size - 2].toIntOrNull()?.let(::Season)
                episode = parts.last().toIntOrNull()?.let(::Episode)
            }
            val isEpisode = season != null && episode != null
            return PlaybackTarget(
                source = source,
                title = title,
                contentId = if (isEpisode) parts.dropLast(2).joinToString(":") else id,
                contentType = type,
                videoId = if (isEpisode) id else null,
                season = if (isEpisode) season else null,
                episode = if (isEpisode) episode else null,
                httpHeaders = httpHeaders,
            )
        }

        /** A source with no account identity (the `--play` development shortcut). */
        fun raw(source: String) = PlaybackTarget(source = source, title = source)
    }
}
