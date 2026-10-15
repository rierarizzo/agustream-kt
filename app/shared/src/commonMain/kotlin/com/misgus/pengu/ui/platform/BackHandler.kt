package com.misgus.pengu.ui.platform

import androidx.compose.runtime.Composable

/**
 * Handles the platform's system back affordance (the Android back button or
 * gesture).
 *
 * A no-op where the platform has none, so shared code can call it
 * unconditionally. Handlers stack: the innermost enabled one consumes the event,
 * which is why a dialog or the player (composed on top) gets it before the shell.
 */
@Composable
expect fun PlatformBackHandler(
    enabled: Boolean = true,
    onBack: () -> Unit,
)
