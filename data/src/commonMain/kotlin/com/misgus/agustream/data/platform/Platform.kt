package com.misgus.agustream.data.platform

import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineDispatcher

/** Dispatcher for blocking file I/O. */
expect val ioDispatcher: CoroutineDispatcher

/**
 * Network timeouts shared by every platform engine.
 *
 * A request that never answers must not leave the UI loading forever, so the
 * engines are configured with finite request, connect and socket timeouts.
 */
const val HTTP_REQUEST_TIMEOUT_MS = 30_000L
const val HTTP_CONNECT_TIMEOUT_MS = 15_000L
const val HTTP_SOCKET_TIMEOUT_MS = 30_000L

/** Builds the platform HTTP client (CIO on desktop, OkHttp on Android). */
expect fun createHttpClient(): HttpClient

/** `%APPDATA%\Agustream` on Windows, XDG config elsewhere. */
expect fun configDirectory(): String

/** Reads [path] as UTF-8, or `null` when it does not exist / cannot be read. */
expect fun readTextFile(path: String): String?

/** Writes [text] to [path] as UTF-8, creating parent directories. */
expect fun writeTextFile(
    path: String,
    text: String,
)

/**
 * Restricts [path] to the current user where the platform supports it.
 *
 * Best-effort hardening for the session file: on Windows this is a no-op, and
 * it is not a replacement for an OS keychain.
 */
expect fun hardenFilePermissions(path: String)

/** Deletes [path] when it exists. */
expect fun deleteFile(path: String)
