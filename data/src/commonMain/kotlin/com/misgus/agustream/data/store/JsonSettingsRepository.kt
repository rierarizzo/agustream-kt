package com.misgus.agustream.data.store

import com.misgus.agustream.core.repository.AppSettings
import com.misgus.agustream.core.repository.AppTheme
import com.misgus.agustream.core.repository.PosterSize
import com.misgus.agustream.core.repository.SettingsRepository
import com.misgus.agustream.data.boolOrNull
import com.misgus.agustream.data.stringOrNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * [SettingsRepository] backed by a flat JSON file, like VS Code's
 * `settings.json`:
 *
 * ```json
 * {
 *   "appearance.theme": "coffee",
 *   "animations.enabled": true,
 *   "player.resumePlayback": true,
 *   "player.autoPlayNext": true
 * }
 * ```
 *
 * Reads are defensive (a missing or corrupt file means defaults) and writes
 * keep any key the app does not know, so the file can be edited by hand.
 */
class JsonSettingsRepository(
    private val store: JsonMapStore,
    override val filePath: String = AppPaths.settingsFile,
) : SettingsRepository {
    private val mutex = Mutex()
    private val _state = MutableStateFlow(AppSettings())
    override val state: StateFlow<AppSettings> = _state.asStateFlow()

    override suspend fun load() =
        mutex.withLock {
            _state.value = store.read().toSettings()
        }

    override suspend fun reload() = load()

    override suspend fun setTheme(theme: AppTheme) = update { it.copy(theme = theme) }

    override suspend fun setAnimationsEnabled(enabled: Boolean) = update { it.copy(animationsEnabled = enabled) }

    override suspend fun setResumePlayback(enabled: Boolean) = update { it.copy(resumePlayback = enabled) }

    override suspend fun setAutoPlayNext(enabled: Boolean) = update { it.copy(autoPlayNext = enabled) }

    override suspend fun setPosterSize(size: PosterSize) = update { it.copy(posterSize = size) }

    override suspend fun setLibraryPosterSize(size: PosterSize) = update { it.copy(libraryPosterSize = size) }

    /** Writes first, then publishes, so a failed save leaves the UI unchanged. */
    private suspend fun update(transform: (AppSettings) -> AppSettings) =
        mutex.withLock {
            val next = transform(_state.value)
            write(next)
            _state.value = next
        }

    private suspend fun write(settings: AppSettings) {
        val data = store.read().toMutableMap()
        data[KEY_THEME] = JsonPrimitive(settings.theme.name.lowercase())
        data[KEY_ANIMATIONS] = JsonPrimitive(settings.animationsEnabled)
        data[KEY_RESUME] = JsonPrimitive(settings.resumePlayback)
        data[KEY_AUTOPLAY] = JsonPrimitive(settings.autoPlayNext)
        data[KEY_POSTER_SIZE] = JsonPrimitive(settings.posterSize.name.lowercase())
        data[KEY_LIBRARY_POSTER_SIZE] = JsonPrimitive(settings.libraryPosterSize.name.lowercase())
        store.write(JsonObject(data))
    }

    private fun JsonObject.toSettings() =
        AppSettings(
            theme = this[KEY_THEME].stringOrNull().toAppTheme() ?: AppTheme.COFFEE,
            animationsEnabled = this[KEY_ANIMATIONS].boolOrNull() ?: true,
            resumePlayback = this[KEY_RESUME].boolOrNull() ?: true,
            autoPlayNext = this[KEY_AUTOPLAY].boolOrNull() ?: true,
            posterSize = this[KEY_POSTER_SIZE].stringOrNull().toPosterSize() ?: PosterSize.DEFAULT,
            libraryPosterSize = this[KEY_LIBRARY_POSTER_SIZE].stringOrNull().toPosterSize() ?: PosterSize.DEFAULT,
        )

    private fun String?.toPosterSize(): PosterSize? =
        when (this?.lowercase()) {
            "small" -> PosterSize.SMALL
            "default" -> PosterSize.DEFAULT
            "large" -> PosterSize.LARGE
            else -> null
        }

    private fun String?.toAppTheme(): AppTheme? = AppTheme.entries.firstOrNull { it.name.equals(this, ignoreCase = true) }

    companion object {
        const val KEY_THEME = "appearance.theme"
        const val KEY_ANIMATIONS = "animations.enabled"
        const val KEY_RESUME = "player.resumePlayback"
        const val KEY_AUTOPLAY = "player.autoPlayNext"
        const val KEY_POSTER_SIZE = "poster.size"
        const val KEY_LIBRARY_POSTER_SIZE = "library.poster.size"
    }
}
