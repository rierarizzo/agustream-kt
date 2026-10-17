package com.misgus.pengu.ui.platform

import androidx.compose.ui.platform.ClipEntry

/**
 * Wraps [text] as the platform [ClipEntry] used by Compose's clipboard API.
 *
 * `ClipEntry` is an expect class with no common constructor or plain-text
 * factory, so each target builds its own (Android wraps `ClipData`, the desktop
 * a `StringSelection`); this keeps the copier in shared code free of platform
 * types.
 */
expect fun clipEntryOfText(text: String): ClipEntry
