package com.misgus.pengu.ui.platform

/** Opens a native file picker for a video file; returns its path or null. */
expect suspend fun pickVideoFile(): String?

/** Opens [path] with the OS default app; returns `false` when it cannot. */
expect suspend fun openInDefaultApp(path: String): Boolean
