package com.misgus.agustream.data.store

import com.misgus.agustream.core.repository.AppSettings
import com.misgus.agustream.core.repository.SettingsRepository
import com.misgus.agustream.data.boolOrNull
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

    override suspend fun setAnimationsEnabled(enabled: Boolean) = update { it.copy(animationsEnabled = enabled) }

    override suspend fun setResumePlayback(enabled: Boolean) = update { it.copy(resumePlayback = enabled) }

    override suspend fun setAutoPlayNext(enabled: Boolean) = update { it.copy(autoPlayNext = enabled) }

    /** Writes first, then publishes, so a failed save leaves the UI unchanged. */
    private suspend fun update(transform: (AppSettings) -> AppSettings) =
        mutex.withLock {
            val next = transform(_state.value)
            write(next)
            _state.value = next
        }

    private suspend fun write(settings: AppSettings) {
        val data = store.read().toMutableMap()
        data[KEY_ANIMATIONS] = JsonPrimitive(settings.animationsEnabled)
        data[KEY_RESUME] = JsonPrimitive(settings.resumePlayback)
        data[KEY_AUTOPLAY] = JsonPrimitive(settings.autoPlayNext)
        store.write(JsonObject(data))
    }

    private fun JsonObject.toSettings() =
        AppSettings(
            animationsEnabled = this[KEY_ANIMATIONS].boolOrNull() ?: true,
            resumePlayback = this[KEY_RESUME].boolOrNull() ?: true,
            autoPlayNext = this[KEY_AUTOPLAY].boolOrNull() ?: true,
        )

    companion object {
        const val KEY_ANIMATIONS = "animations.enabled"
        const val KEY_RESUME = "player.resumePlayback"
        const val KEY_AUTOPLAY = "player.autoPlayNext"
    }
}
