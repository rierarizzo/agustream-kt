package com.misgus.agustream.core.repository

import kotlinx.coroutines.flow.StateFlow

/** Nominal width of a poster, so grids and rows can be consistent. */
enum class PosterSize {
    SMALL,
    DEFAULT,
    LARGE,
}

/** Locally stored preferences. */
data class AppSettings(
    /** Play the app's motion (transitions, hover, skeletons). */
    val animationsEnabled: Boolean = true,
    /** Resume a title from its saved position instead of the start. */
    val resumePlayback: Boolean = true,
    /** Start the next episode automatically when one finishes. */
    val autoPlayNext: Boolean = true,
    /**
     * Poster size for the browse surfaces: the catalog grids, Discover and the
     * "More like this" row, which all share it.
     */
    val posterSize: PosterSize = PosterSize.DEFAULT,
    /** Poster size for the library grid, kept apart from [posterSize]. */
    val libraryPosterSize: PosterSize = PosterSize.DEFAULT,
)

/**
 * Local preferences, backed by a JSON file (`settings.json`).
 *
 * The file is the source of truth and can be edited by hand, like VS Code's
 * `settings.json`; [reload] re-reads it. Unknown keys in the file are preserved
 * on write, so manual additions are not clobbered. [filePath] is exposed so the
 * settings screen can show or open it.
 */
interface SettingsRepository {
    val state: StateFlow<AppSettings>

    /** Path of the backing file, for display or opening in an editor. */
    val filePath: String

    /** Loads the file into [state]; call once at startup. */
    suspend fun load()

    /** Re-reads the file, picking up manual edits. */
    suspend fun reload()

    suspend fun setAnimationsEnabled(enabled: Boolean)

    suspend fun setResumePlayback(enabled: Boolean)

    suspend fun setAutoPlayNext(enabled: Boolean)

    suspend fun setPosterSize(size: PosterSize)

    suspend fun setLibraryPosterSize(size: PosterSize)
}
