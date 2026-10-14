package com.misgus.agustream.data.platform

import android.content.Context
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import kotlinx.coroutines.Dispatchers
import java.io.File

actual val ioDispatcher = Dispatchers.IO

private var applicationContext: Context? = null

/**
 * Wires the platform layer to the app [context].
 *
 * Called once from the Android entry point; without it the app falls back to a
 * temporary directory, which does not survive a process restart.
 */
fun initPlatform(context: Context) {
    applicationContext = context.applicationContext
}

actual fun createHttpClient(): HttpClient =
    HttpClient(OkHttp) {
        expectSuccess = false
        install(HttpTimeout) {
            requestTimeoutMillis = HTTP_REQUEST_TIMEOUT_MS
            connectTimeoutMillis = HTTP_CONNECT_TIMEOUT_MS
            socketTimeoutMillis = HTTP_SOCKET_TIMEOUT_MS
        }
    }

actual fun configDirectory(): String {
    val context =
        applicationContext
            ?: return File(System.getProperty("java.io.tmpdir") ?: ".", "Agustream").path
    return File(context.filesDir, "Agustream").path
}

actual fun readTextFile(path: String): String? = PlatformFileIO.read(path)

actual fun writeTextFile(
    path: String,
    text: String,
) = PlatformFileIO.write(path, text)

actual fun deleteFile(path: String) = PlatformFileIO.delete(path)

actual fun hardenFilePermissions(path: String) {
    val file = File(path)
    if (!file.exists()) return
    runCatching {
        file.setReadable(false, false)
        file.setReadable(true, true)
        file.setWritable(false, false)
        file.setWritable(true, true)
    }
}
