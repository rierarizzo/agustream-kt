package com.misgus.agustream.data.platform

import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineDispatcher

/** Dispatcher for blocking file I/O. */
expect val ioDispatcher: CoroutineDispatcher

/** Builds the platform HTTP client (CIO on desktop, OkHttp on Android). */
expect fun createHttpClient(): HttpClient

/** `%APPDATA%\Agustream` on Windows, XDG config elsewhere. */
expect fun configDirectory(): String

/** Reads [path] as UTF-8, or `null` when it does not exist / cannot be read. */
expect fun readTextFile(path: String): String?

/** Writes [text] to [path] as UTF-8, creating parent directories. */
expect fun writeTextFile(path: String, text: String)

/** Deletes [path] when it exists. */
expect fun deleteFile(path: String)
