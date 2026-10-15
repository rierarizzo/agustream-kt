package com.misgus.pengu.data.platform

import java.io.File
import java.io.IOException

/**
 * File helpers shared by the JVM and Android targets.
 *
 * Both targets back the platform layer with `java.io.File`, so the reading,
 * atomic writing and deleting live here once instead of being duplicated in
 * each `actual` file. Only the parts that genuinely differ per platform
 * (dispatcher, config directory, permission hardening) stay in the leaves.
 */
internal object PlatformFileIO {
    /** Reads [path] as UTF-8, or `null` when it does not exist / cannot be read. */
    fun read(path: String): String? =
        runCatching {
            val file = File(path)
            if (file.exists()) file.readText(Charsets.UTF_8) else null
        }.getOrNull()

    /**
     * Writes [text] to [path] atomically.
     *
     * The content goes to a sibling temporary file that is then renamed over the
     * target, so an interrupted write cannot leave a truncated document. A
     * failure is thrown instead of swallowed: the caller must know the data was
     * not saved.
     */
    fun write(
        path: String,
        text: String,
    ) {
        val file = File(path)
        file.parentFile?.mkdirs()
        val temporary = File(file.parentFile, "${file.name}.tmp")
        temporary.writeText(text, Charsets.UTF_8)
        if (!temporary.renameTo(file)) {
            // Windows refuses to rename over an existing file; replace it explicitly.
            if (file.exists() && !file.delete()) {
                temporary.delete()
                throw IOException("Could not replace $path")
            }
            if (!temporary.renameTo(file)) {
                file.writeText(text, Charsets.UTF_8)
                temporary.delete()
            }
        }
    }

    /** Deletes [path] when it exists; never throws. */
    fun delete(path: String) {
        runCatching {
            val file = File(path)
            if (file.exists()) file.delete()
        }
    }

    /**
     * Moves the legacy `Agustream` config directory to its `Pengu` name once.
     *
     * [legacy] and [current] are siblings, so the rename normally succeeds on
     * every platform. The copy fallback covers a locked file or a different
     * mount; it stages the copy next to the target so an interrupted transfer is
     * not mistaken for a finished migration on the next start. A failed
     * migration leaves the legacy directory untouched, since no data is lost by
     * keeping it around.
     */
    fun migrateConfigDirectory(
        legacy: File,
        current: File,
    ) {
        if (current.exists() || !legacy.exists()) return
        runCatching {
            current.parentFile?.mkdirs()
            if (legacy.renameTo(current)) return@runCatching
            val staging = File(current.parentFile, "${current.name}.migrating")
            staging.deleteRecursively()
            legacy.copyRecursively(staging, overwrite = false)
            if (!staging.renameTo(current)) {
                staging.deleteRecursively()
                throw IOException("Could not migrate ${legacy.path} to ${current.path}")
            }
        }
    }
}
