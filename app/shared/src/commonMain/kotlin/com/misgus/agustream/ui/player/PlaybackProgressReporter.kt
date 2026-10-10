package com.misgus.agustream.ui.player

import com.misgus.agustream.core.model.WatchProgress
import com.misgus.agustream.core.player.PlaybackTarget
import com.misgus.agustream.core.repository.ProgressRepository
import com.misgus.agustream.core.util.runCatchingCancellable
import kotlin.time.Clock
import kotlin.time.Duration

/**
 * Saves watch progress for the playing [PlaybackTarget].
 *
 * Writes at most once per [INTERVAL] while playing, plus on pause, completion
 * and exit. A target without a content id (the `--play` shortcut) is ignored.
 */
class PlaybackProgressReporter(
    private val repository: ProgressRepository,
    private val target: PlaybackTarget,
) {
    private var position: Duration = Duration.ZERO
    private var duration: Duration = Duration.ZERO
    private var lastSaved: Duration = Duration.ZERO
    private var resumeResolved = false

    /** Saved position to resume from, when it is worth resuming. */
    suspend fun resumePosition(): Duration? {
        if (!target.canReportProgress || resumeResolved) return null
        resumeResolved = true
        return runCatchingCancellable {
            val entry = find(repository.all())
            val savedPosition = entry?.position
            if (savedPosition == null || savedPosition <= Duration.ZERO) return@runCatchingCancellable null
            val savedDuration = entry.duration
            if (savedDuration != null && savedDuration > Duration.ZERO) {
                val fraction =
                    savedPosition.inWholeMilliseconds.toDouble() /
                        savedDuration.inWholeMilliseconds
                if (fraction < RESUME_MIN_FRACTION || fraction > RESUME_MAX_FRACTION) {
                    return@runCatchingCancellable null
                }
            } else if (savedPosition < Duration.parse("5s")) {
                return@runCatchingCancellable null
            }
            savedPosition
        }.getOrNull()
    }

    /** Records the latest playback state. [flush] persists it when it is due. */
    fun update(
        position: Duration,
        duration: Duration,
    ) {
        this.position = position
        this.duration = duration
    }

    /** Persists the current state, unless [force] when it is too soon. */
    suspend fun flush(force: Boolean = false) {
        if (!target.canReportProgress) return
        if (duration <= Duration.ZERO) return
        if (!force && (position - lastSaved).absoluteValue < INTERVAL) return
        if (position <= Duration.ZERO && lastSaved <= Duration.ZERO) return
        lastSaved = position
        runCatchingCancellable {
            repository.save(
                WatchProgress(
                    id = "",
                    contentId = target.contentId!!,
                    contentType = target.contentType ?: "movie",
                    videoId = target.videoId,
                    season = target.season,
                    episode = target.episode,
                    position = position,
                    duration = duration,
                    lastWatched = Clock.System.now(),
                    progressKey = target.progressKey,
                ),
            )
        }
    }

    private fun find(entries: List<WatchProgress>): WatchProgress? {
        val key = target.progressKey
        for (entry in entries) {
            if (key != null && entry.progressKey == key) return entry
            if (entry.contentId == target.contentId &&
                entry.season == target.season &&
                entry.episode == target.episode
            ) {
                return entry
            }
        }
        return null
    }

    private companion object {
        val INTERVAL = Duration.parse("10s")
        const val RESUME_MIN_FRACTION = 0.02
        const val RESUME_MAX_FRACTION = 0.90
    }
}
