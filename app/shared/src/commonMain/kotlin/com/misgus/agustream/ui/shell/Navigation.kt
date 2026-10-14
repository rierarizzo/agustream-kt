package com.misgus.agustream.ui.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.misgus.agustream.core.model.BackendProfile
import com.misgus.agustream.ui.components.ProfileAvatar
import com.misgus.agustream.ui.components.clickableSurface
import com.misgus.agustream.ui.components.focusRing
import com.misgus.agustream.ui.components.interactionHighlight
import com.misgus.agustream.ui.components.rememberInteraction
import com.misgus.agustream.ui.theme.AppAlpha
import com.misgus.agustream.ui.theme.AppIconSize
import com.misgus.agustream.ui.theme.AppSizes
import com.misgus.agustream.ui.theme.AppSpacing

/**
 * Vertical navigation rail for wide windows, with the profile and section items
 * centered in the window.
 *
 * It floats over the content, which reserves the rail's width, so showing or
 * hiding it never resizes the content viewport. The items are icon-only — at
 * [AppSizes.railWidth] the icons alone stay legible and the rail stays narrow —
 * and the container paints the page background, so the rail blends into the
 * window instead of reading as a separate panel.
 */
@Composable
fun LeftNav(
    selected: AppSection,
    onSelected: (AppSection) -> Unit,
    activeProfile: BackendProfile? = null,
) {
    Box(
        modifier =
            Modifier
                .fillMaxHeight()
                .width(AppSizes.railWidth)
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
                .navigationBarsPadding(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            ProfileAvatar(
                profile = activeProfile,
                size = 32.dp,
                initialStyle = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(AppSpacing.sm))
            AppSection.entries.forEach { section ->
                RailNavItem(section, section == selected) { onSelected(section) }
            }
        }
    }
}

/**
 * Icon-only bottom navigation for compact windows.
 *
 * A bar with labels does not fit a phone; the icons alone do, and the bar sits
 * in reserved space instead of floating over the content.
 */
@Composable
fun BottomNav(
    selected: AppSection,
    onSelected: (AppSection) -> Unit,
) {
    // The container carries the surface and the navigation-bar inset, so the bar
    // itself adds neither and the background reaches the window edge.
    Box(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .navigationBarsPadding(),
    ) {
        NavigationBar(
            modifier = Modifier.fillMaxWidth(),
            containerColor = Color.Transparent,
            tonalElevation = 0.dp,
            // The container above already handles the system bars.
            windowInsets = WindowInsets(0, 0, 0, 0),
        ) {
            AppSection.entries.forEach { section ->
                NavigationBarItem(
                    selected = section == selected,
                    onClick = { onSelected(section) },
                    // Match the rail's selected treatment (translucent on-surface
                    // fill) instead of Material's default secondary container.
                    colors =
                        NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onSurface,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = AppAlpha.NAV_SELECTED),
                        ),
                    icon = {
                        Icon(
                            imageVector = if (section == selected) section.selectedIcon else section.icon,
                            contentDescription = section.label,
                            modifier = Modifier.size(AppIconSize.lg),
                        )
                    },
                )
            }
        }
    }
}

/** One section in the rail: an icon-only target with a translucent selected fill. */
@Composable
private fun RailNavItem(
    section: AppSection,
    selected: Boolean,
    onTap: () -> Unit,
) {
    val interaction = rememberInteraction()
    val isSelected = selected
    val background =
        interactionHighlight(
            interaction,
            base =
                if (selected) {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = AppAlpha.NAV_SELECTED)
                } else {
                    Color.Transparent
                },
        )
    val contentColor =
        if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier =
            Modifier
                .size(AppSizes.minTouchTarget)
                .focusRing(interaction)
                .clip(MaterialTheme.shapes.small)
                .background(background)
                .clickableSurface(interaction, onTap)
                .semantics { this.selected = isSelected },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = if (selected) section.selectedIcon else section.icon,
            contentDescription = section.label,
            tint = contentColor,
            modifier = Modifier.size(AppIconSize.sm),
        )
    }
}
