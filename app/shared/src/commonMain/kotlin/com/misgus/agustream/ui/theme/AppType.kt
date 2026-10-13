package com.misgus.agustream.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

/**
 * Type roles that step down one size on a compact window.
 *
 * Material's `display*` roles and the largest `headline` sizes are sized for a
 * big screen; on a phone they tower over the layout, so the next role down is
 * used there. Desktop and tablet keep the full scale, so nothing changes for
 * them.
 */
object AppType {
    /** Title of a hero (a movie or series name). */
    @Composable
    fun heroTitle(): TextStyle =
        if (LocalWindowSize.current == WindowSize.COMPACT) {
            MaterialTheme.typography.headlineLarge
        } else {
            MaterialTheme.typography.displayMedium
        }

    /** Title of a full-screen state: onboarding, profile picker, splash. */
    @Composable
    fun screenTitle(): TextStyle =
        if (LocalWindowSize.current == WindowSize.COMPACT) {
            MaterialTheme.typography.headlineMedium
        } else {
            MaterialTheme.typography.headlineLarge
        }

    /** Hero synopsis and other long copy over artwork. */
    @Composable
    fun heroBody(): TextStyle =
        if (LocalWindowSize.current == WindowSize.COMPACT) {
            MaterialTheme.typography.bodyLarge
        } else {
            MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp, lineHeight = 26.sp)
        }
}
