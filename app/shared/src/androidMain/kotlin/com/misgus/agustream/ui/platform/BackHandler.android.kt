package com.misgus.agustream.ui.platform

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable

/** Android routes system back through the activity's back dispatcher. */
@Composable
actual fun PlatformBackHandler(
    enabled: Boolean,
    onBack: () -> Unit,
) {
    BackHandler(enabled = enabled, onBack = onBack)
}
