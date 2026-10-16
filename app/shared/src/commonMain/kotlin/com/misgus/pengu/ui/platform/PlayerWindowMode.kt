package com.misgus.pengu.ui.platform

import androidx.compose.runtime.Composable

/**
 * Puts the platform into player mode while it is composed, restoring the
 * previous state on dispose.
 *
 * On Android that means landscape-only (a phone has no sensible portrait layout
 * for video) and immersive: the system bars are hidden so playback fills the
 * screen. On desktop it is a no-op, because there the player is a resizable,
 * decorated window and fullscreen is an explicit user toggle.
 */
@Composable
expect fun PlayerWindowMode()
