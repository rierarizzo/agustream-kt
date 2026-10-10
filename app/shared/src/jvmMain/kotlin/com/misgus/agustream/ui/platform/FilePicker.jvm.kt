package com.misgus.agustream.ui.platform

import java.awt.FileDialog
import java.awt.Frame
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val videoExtensions = setOf("mp4", "mkv", "webm", "mov", "avi", "m4v")

actual suspend fun pickVideoFile(): String? = withContext(Dispatchers.IO) {
    val dialog = FileDialog(null as Frame?, "Open video", FileDialog.LOAD)
    dialog.setFilenameFilter { _, name ->
        name.substringAfterLast('.', "").lowercase() in videoExtensions
    }
    dialog.isVisible = true
    val file = dialog.file ?: return@withContext null
    val directory = dialog.directory ?: return@withContext null
    "$directory$file"
}
