package com.misgus.agustream.ui.platform

/** Opens a native file picker for a video file; returns its path or null. */
expect suspend fun pickVideoFile(): String?
