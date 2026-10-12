package com.misgus.agustream.ui.platform

/** No native file picker wired up on Android yet. */
actual suspend fun pickVideoFile(): String? = null

/** Opening a local file with an external app is not wired up on Android yet. */
actual suspend fun openInDefaultApp(path: String): Boolean = false
