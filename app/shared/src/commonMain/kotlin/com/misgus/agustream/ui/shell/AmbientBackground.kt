package com.misgus.agustream.ui.shell

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.misgus.agustream.ui.services.LocalAppServices

/**
 * Registers the ambient background colour for the current screen.
 *
 * The tint is applied while this composable stays in the composition and cleared
 * when it leaves, so screens without artwork (Library, Settings, ...) fall back
 * to a neutral background. Ownership is tracked, so a screen being disposed does
 * not wipe the tint already set by the screen replacing it (e.g. Home → Detail).
 *
 * A `null` [color] means "not known yet": the previous tint is kept until the
 * artwork has been sampled.
 */
@Composable
fun AmbientBackground(color: Color?) {
    val ambient = LocalAppServices.current.ambient
    val owner = remember { Any() }
    DisposableEffect(owner) {
        onDispose { ambient.clear(owner) }
    }
    LaunchedEffect(owner, color) {
        if (color != null) ambient.set(owner, color)
    }
}
