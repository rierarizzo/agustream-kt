package com.misgus.agustream.ui.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.misgus.agustream.core.model.BackendProfile
import com.misgus.agustream.ui.components.ProfileAvatar
import com.misgus.agustream.ui.components.clickableSurface
import com.misgus.agustream.ui.components.interactionHighlight
import com.misgus.agustream.ui.components.rememberInteraction
import com.misgus.agustream.ui.theme.AppAlpha
import com.misgus.agustream.ui.theme.AppColors
import com.misgus.agustream.ui.theme.AppIconSize
import com.misgus.agustream.ui.theme.AppRadii
import com.misgus.agustream.ui.theme.AppScrim
import com.misgus.agustream.ui.theme.AppSizes
import com.misgus.agustream.ui.theme.AppSpacing
import com.misgus.agustream.ui.theme.LocalWindowSize
import com.misgus.agustream.ui.theme.screenPadding

/**
 * Horizontal navigation bar for wide windows, with the profile and section items
 * centered. It floats over the content, which reserves its height, so showing or
 * hiding it never resizes the content viewport.
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
                // Over the full-bleed Home billboard the bar needs its own dark
                // strip; over the plain section background it is invisible.
                .background(AppScrim.detailTop)
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
                NavItem(section, section == selected) { onSelected(section) }
            }
        }
    }
}

/**
 * Vertical navigation rail for wide windows.
 *
 * Opaque and left-aligned, with the profile and the section items centered in
 * the rail. It floats over the content, which reserves the rail's width, so
 * showing or hiding it never resizes the content viewport.
 */
@Composable
fun LeftNav(
    selected: AppSection,
    onSelected: (AppSection) -> Unit,
    activeProfile: BackendProfile? = null,
) {
    // Same background as the shell, so the rail reads as part of the page
    // instead of a raised panel with its own edge.
    Box(Modifier.fillMaxHeight().width(AppSizes.railWidth).background(AppColors.background)) {
        Column(
            modifier =
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(vertical = AppSpacing.md),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ProfileAvatar(
                profile = activeProfile,
                size = 32.dp,
                initialStyle = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(AppSpacing.lg))
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
    Column(Modifier.fillMaxWidth().background(AppColors.background).navigationBarsPadding()) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(AppColors.divider))
        Row(
            modifier = Modifier.fillMaxWidth().height(AppSizes.bottomBarHeight),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppSection.entries.forEach { section ->
                BottomNavItem(section, section == selected) { onSelected(section) }
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    section: AppSection,
    selected: Boolean,
    onTap: () -> Unit,
) {
    val interaction = rememberInteraction()
    Box(
        modifier =
            Modifier
                .clip(RoundedCornerShape(AppRadii.md))
                .background(interactionHighlight(interaction))
                .clickableSurface(interaction, onTap)
                .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
    ) {
        Icon(
            imageVector = if (selected) section.selectedIcon else section.icon,
            contentDescription = section.label,
            tint = if (selected) AppColors.textPrimary else AppColors.textSecondary,
            modifier = Modifier.size(AppIconSize.lg),
        )
    }
}

@Composable
private fun RailNavItem(
    section: AppSection,
    selected: Boolean,
    onTap: () -> Unit,
) {
    val interaction = rememberInteraction()
    val background =
        interactionHighlight(
            interaction,
            base = if (selected) Color.White.copy(alpha = AppAlpha.NAV_SELECTED) else Color.Transparent,
        )
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs)
                .clip(RoundedCornerShape(AppRadii.md))
                .background(background)
                .clickableSurface(interaction, onTap)
                .padding(vertical = AppSpacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = if (selected) section.selectedIcon else section.icon,
            contentDescription = section.label,
            tint = if (selected) AppColors.textPrimary else AppColors.textSecondary,
            modifier = Modifier.size(AppIconSize.lg),
        )
        Spacer(Modifier.height(AppSpacing.xs))
        Text(
            text = section.label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) AppColors.textPrimary else AppColors.textSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun NavItem(
    section: AppSection,
    selected: Boolean,
    onTap: () -> Unit,
) {
    val interaction = rememberInteraction()
    val background =
        interactionHighlight(
            interaction,
            base = if (selected) Color.White.copy(alpha = AppAlpha.NAV_SELECTED) else Color.Transparent,
        )
    Row(
        modifier =
            Modifier
                .padding(horizontal = AppSpacing.xs)
                .clip(RoundedCornerShape(AppRadii.md))
                .background(background)
                .clickableSurface(interaction, onTap)
                .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (selected) section.selectedIcon else section.icon,
            contentDescription = section.label,
            tint = if (selected) AppColors.textPrimary else AppColors.textSecondary,
            modifier = Modifier.size(AppIconSize.sm),
        )
        Spacer(Modifier.width(AppSpacing.sm))
        Text(
            text = section.label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) AppColors.textPrimary else AppColors.textSecondary,
        )
    }
}
