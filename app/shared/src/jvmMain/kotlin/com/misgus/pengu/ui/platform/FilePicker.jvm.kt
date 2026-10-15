package com.misgus.pengu.ui.platform

import com.misgus.pengu.app.shared.generated.resources.Res
import com.misgus.pengu.app.shared.generated.resources.dialog_open_video
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.getString
import java.awt.FileDialog
import java.awt.Frame
import javax.swing.SwingUtilities

private val videoExtensions = setOf("mp4", "mkv", "webm", "mov", "avi", "m4v")

/**
 * Opens a native file dialog and returns the chosen path, or `null`.
 *
 * AWT dialogs must be created and shown on the Event Dispatch Thread, so the
 * blocking modal call runs there while this coroutine waits on the IO
 * dispatcher.
 */
actual suspend fun pickVideoFile(): String? {
    val title = getString(Res.string.dialog_open_video)
    return withContext(Dispatchers.IO) {
        var selected: String? = null
        SwingUtilities.invokeAndWait {
            val dialog = FileDialog(null as Frame?, title, FileDialog.LOAD)
            dialog.setFilenameFilter { _, name ->
                name.substringAfterLast('.', "").lowercase() in videoExtensions
            }
            dialog.isVisible = true
            val file = dialog.file
            val directory = dialog.directory
            selected = if (file != null && directory != null) "$directory$file" else null
        }
        selected
    }
}

/** Opens the file with the OS default app through AWT's [java.awt.Desktop]. */
actual suspend fun openInDefaultApp(path: String): Boolean =
    withContext(Dispatchers.IO) {
        runCatching {
            val desktop = java.awt.Desktop.getDesktop()
            desktop.isSupported(java.awt.Desktop.Action.OPEN) &&
                run {
                    desktop.open(java.io.File(path))
                    true
                }
        }.getOrDefault(false)
    }
