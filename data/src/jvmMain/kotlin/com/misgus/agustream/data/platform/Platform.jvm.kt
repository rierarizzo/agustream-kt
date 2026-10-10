package com.misgus.agustream.data.platform

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import kotlinx.coroutines.Dispatchers
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.attribute.PosixFileAttributeView
import java.nio.file.attribute.PosixFilePermissions

actual val ioDispatcher = Dispatchers.IO

actual fun createHttpClient(): HttpClient =
    HttpClient(CIO) {
        expectSuccess = false
        install(HttpTimeout) {
            requestTimeoutMillis = HTTP_REQUEST_TIMEOUT_MS
            connectTimeoutMillis = HTTP_CONNECT_TIMEOUT_MS
            socketTimeoutMillis = HTTP_SOCKET_TIMEOUT_MS
        }
    }

actual fun configDirectory(): String {
    val env = System.getenv()
    val isWindows = System.getProperty("os.name").lowercase().contains("win")
    val base =
        if (isWindows) {
            env["APPDATA"] ?: env["LOCALAPPDATA"] ?: "."
        } else {
            env["XDG_CONFIG_HOME"] ?: "${env["HOME"] ?: "."}/.config"
        }
    return File(base, "Agustream").path
}

actual fun readTextFile(path: String): String? =
    runCatching {
        val file = File(path)
        if (file.exists()) file.readText(Charsets.UTF_8) else null
    }.getOrNull()

/**
 * Writes [text] to [path] atomically.
 *
 * The content goes to a sibling temporary file that is then renamed over the
 * target, so an interrupted write cannot leave a truncated document. A failure
 * is thrown instead of swallowed: the caller must know the data was not saved.
 */
actual fun writeTextFile(
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

actual fun deleteFile(path: String) {
    runCatching {
        val file = File(path)
        if (file.exists()) file.delete()
    }
}

actual fun hardenFilePermissions(path: String) {
    val file = File(path)
    if (!file.exists()) return
    runCatching {
        // Only meaningful on POSIX filesystems; on Windows this is a no-op.
        val view = Files.getFileAttributeView(file.toPath(), PosixFileAttributeView::class.java)
        if (view != null) {
            Files.setPosixFilePermissions(
                file.toPath(),
                PosixFilePermissions.fromString("rw-------"),
            )
        }
    }
}
