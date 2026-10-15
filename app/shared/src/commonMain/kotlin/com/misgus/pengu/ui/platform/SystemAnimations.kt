package com.misgus.pengu.ui.platform

/**
 * Whether the platform's "reduce motion" switch allows animating.
 *
 * The in-app setting is combined with this at the root, so the OS accessibility
 * switch wins even when the app setting is on. Returns `true` where the platform
 * has no such signal (desktop).
 */
expect fun systemAnimationsEnabled(): Boolean
