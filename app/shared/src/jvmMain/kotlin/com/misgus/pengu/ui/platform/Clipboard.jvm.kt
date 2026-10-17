package com.misgus.pengu.ui.platform

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ClipEntry
import java.awt.datatransfer.StringSelection

@OptIn(ExperimentalComposeUiApi::class)
actual fun clipEntryOfText(text: String): ClipEntry = ClipEntry(StringSelection(text))
