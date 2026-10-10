package com.misgus.agustream.data.platform

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import kotlinx.coroutines.Dispatchers
import java.io.File

actual val ioDispatcher = Dispatchers.IO

actual fun createHttpClient(): HttpClient = HttpClient(CIO) {
    expectSuccess = false
}

actual fun configDirectory(): String {
    val env = System.getenv()
    val isWindows = System.getProperty("os.name").lowercase().contains("win")
    val base = if (isWindows) {
        env["APPDATA"] ?: env["LOCALAPPDATA"] ?: "."
    } else {
        env["XDG_CONFIG_HOME"] ?: "${env["HOME"] ?: "."}/.config"
    }
    return File(base, "Agustream").path
}

actual fun readTextFile(path: String): String? = runCatching {
    val file = File(path)
    if (file.exists()) file.readText(Charsets.UTF_8) else null
}.getOrNull()

actual fun writeTextFile(path: String, text: String) {
    runCatching {
        val file = File(path)
        file.parentFile?.mkdirs()
        file.writeText(text, Charsets.UTF_8)
    }
}

actual fun deleteFile(path: String) {
    runCatching {
        val file = File(path)
        if (file.exists()) file.delete()
    }
}
