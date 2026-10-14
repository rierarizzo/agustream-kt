package com.misgus.agustream.ui.player

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/** Seek step used by the keyboard shortcuts and the control buttons. */
internal val SEEK_BACK: Duration = (-10).seconds
internal val SEEK_FORWARD: Duration = 10.seconds
