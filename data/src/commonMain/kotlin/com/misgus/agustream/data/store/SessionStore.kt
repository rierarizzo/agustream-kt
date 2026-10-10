package com.misgus.agustream.data.store

import com.misgus.agustream.data.objOrNull
import com.misgus.agustream.data.platform.configDirectory
import com.misgus.agustream.data.platform.deleteFile
import com.misgus.agustream.data.platform.hardenFilePermissions
import com.misgus.agustream.data.platform.ioDispatcher
import com.misgus.agustream.data.platform.readTextFile
import com.misgus.agustream.data.platform.writeTextFile
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject

/** Well-known paths inside the app config directory. */
object AppPaths {
    val configDir: String get() = configDirectory()
    val sessionFile: String get() = "$configDir/session.json"
    val localStoreDir: String get() = "$configDir/local"
    val watchedSeriesCacheFile: String get() = "$configDir/watched_series.json"
    val ratingsFile: String get() = "$configDir/ratings.json"
}

/**
 * Persistence for the signed-in session.
 *
 * Kept behind an interface so the transport does not know where the session
 * lives: tests use [NoopSessionStore], while the app uses [FileSessionStore].
 */
interface SessionStore {
    suspend fun read(): JsonObject?

    suspend fun write(session: JsonObject)

    suspend fun clear()
}

/** Store that keeps nothing. The session lives only in memory. */
class NoopSessionStore : SessionStore {
    override suspend fun read(): JsonObject? = null

    override suspend fun write(session: JsonObject) = Unit

    override suspend fun clear() = Unit
}

/**
 * Stores the session as a JSON file under the user's config directory.
 *
 * Failures are swallowed: a corrupted or unreadable file degrades to "no
 * session", which means signing in again, instead of crashing the startup.
 */
class FileSessionStore(
    private val path: String = AppPaths.sessionFile,
) : SessionStore {
    override suspend fun read(): JsonObject? =
        withContext(ioDispatcher) {
            val text = readTextFile(path) ?: return@withContext null
            runCatching {
                com.misgus.agustream.data.appJson
                    .parseToJsonElement(text)
                    .objOrNull()
            }.getOrNull()
        }

    override suspend fun write(session: JsonObject) =
        withContext(ioDispatcher) {
            writeTextFile(path, session.toString())
            // Restrict the file that holds the refresh token where the OS allows it.
            hardenFilePermissions(path)
        }

    override suspend fun clear() =
        withContext(ioDispatcher) {
            deleteFile(path)
        }
}
