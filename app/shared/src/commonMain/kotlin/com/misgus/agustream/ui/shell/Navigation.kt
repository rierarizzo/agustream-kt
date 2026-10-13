package com.misgus.agustream.ui.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.misgus.agustream.core.model.BackendProfile
import com.misgus.agustream.ui.components.ProfileAvatar
import com.misgus.agustream.ui.components.clickableSurface
import com.misgus.agustream.ui.components.interactionHighlight
import com.misgus.agustream.ui.components.rememberInteraction
import com.misgus.agustream.ui.theme.AppAlpha
import com.misgus.agustream.ui.theme.AppIconSize
import com.misgus.agustream.ui.theme.AppSizes
import com.misgus.agustream.ui.theme.AppSpacing
import com.misgus.agustream.ui.theme.LocalWindowSize
import com.misgus.agustream.ui.theme.screenPadding

/**
 * Horizontal navigation bar for wide windows, with the profile and section items
 * centered. It floats over the content, which reserves its height, so showing or
 * hiding it never resizes the content viewport.
 *
 * Material 3 has no multi-destination top bar, so the items are custom; they
 * still use the Material navigation colours, so a selected item reads the same
 * as on the bar and the rail.
 */
@Composable
fun TopNav(
    selected: AppSection,
    onSelected: (AppSection) -> Unit,
    activeProfile: BackendProfile? = null,
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .horizontalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier.height(AppSizes.topBarHeight).padding(horizontal = LocalWindowSize.current.screenPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ProfileAvatar(
                profile = activeProfile,
                size = 32.dp,
                initialStyle = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.width(AppSpacing.lg))
            AppSection.entries.forEach { section ->
                TopNavItem(section, section == selected) { onSelected(section) }
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

/** One section in the custom top bar, with a translucent selected fill. */
@Composable
private fun TopNavItem(
    section: AppSection,
    selected: Boolean,
    onTap: () -> Unit,
) {
    val interaction = rememberInteraction()
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
    Row(
        modifier =
            Modifier
                .sizeIn(minWidth = AppSizes.minTouchTarget, minHeight = AppSizes.minTouchTarget)
                .padding(horizontal = AppSpacing.xs)
                .clip(MaterialTheme.shapes.small)
                .background(background)
                .clickableSurface(interaction, onTap)
                .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (selected) section.selectedIcon else section.icon,
            contentDescription = section.label,
            tint = contentColor,
            modifier = Modifier.size(AppIconSize.sm),
        )
        Spacer(Modifier.width(AppSpacing.sm))
        Text(
            text = section.label,
            style = MaterialTheme.typography.labelLarge,
            color = contentColor,
        )
    }
}
