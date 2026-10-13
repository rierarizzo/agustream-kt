package com.misgus.agustream.ui.platform

import androidx.compose.runtime.Composable

/** Desktop has no system back affordance; the on-screen back buttons handle it. */
@Composable
actual fun PlatformBackHandler(
    enabled: Boolean,
    onBack: () -> Unit,
) = Unit
