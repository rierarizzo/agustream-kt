package com.misgus.pengu.ui.platform

import android.content.ClipData
import androidx.compose.ui.platform.ClipEntry

actual fun clipEntryOfText(text: String): ClipEntry = ClipEntry(ClipData.newPlainText(null, text))
