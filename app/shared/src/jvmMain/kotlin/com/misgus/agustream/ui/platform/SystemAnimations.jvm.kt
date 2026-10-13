package com.misgus.agustream.ui.platform

/** Desktop has no standard reduce-motion signal; the in-app setting is the switch. */
actual fun systemAnimationsEnabled(): Boolean = true
