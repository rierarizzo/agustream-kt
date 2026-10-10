package com.misgus.agustream.ui.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.misgus.agustream.core.model.BackendProfile
import com.misgus.agustream.ui.components.PosterImage
import com.misgus.agustream.ui.theme.AppColors
import com.misgus.agustream.ui.theme.AppRadii
import com.misgus.agustream.ui.theme.AppSizes
import com.misgus.agustream.ui.theme.AppSpacing

/** Icon-only navigation rail, fixed to the left edge of the shell. */
@Composable
fun SideRail(
    selected: AppSection,
    onSelected: (AppSection) -> Unit,
    activeProfile: BackendProfile? = null,
) {
    Column(
        modifier =
            Modifier
                .width(AppSizes.sideRailWidth)
                .fillMaxHeight()
                .background(AppColors.background),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.size(AppSpacing.sm))
        ProfileAvatar(activeProfile)
        Spacer(Modifier.size(AppSpacing.md))
        AppSection.entries
            .filter { it != AppSection.SETTINGS }
            .forEach { section ->
                RailButton(section, section == selected) { onSelected(section) }
            }
        Spacer(Modifier.weight(1f))
        RailButton(AppSection.SETTINGS, selected == AppSection.SETTINGS) {
            onSelected(AppSection.SETTINGS)
        }
        Spacer(Modifier.size(AppSpacing.sm))
    }
}

@Composable
private fun RailButton(
    section: AppSection,
    selected: Boolean,
    onTap: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .padding(vertical = AppSpacing.xs)
                .size(AppSizes.railButton)
                .clip(RoundedCornerShape(AppRadii.md))
                .background(if (selected) AppColors.surfaceHigh else Color.Transparent)
                .clickable(onClick = onTap),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = if (selected) section.selectedIcon else section.icon,
            contentDescription = section.label,
            tint = if (selected) AppColors.textPrimary else AppColors.textSecondary,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun ProfileAvatar(profile: BackendProfile?) {
    val color = profile?.avatarColorHex?.let(::parseHex) ?: AppColors.surfaceHigh
    Box(
        modifier =
            Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(color),
        contentAlignment = Alignment.Center,
    ) {
        val avatarUrl = profile?.avatarUrl
        when {
            !avatarUrl.isNullOrEmpty() && avatarUrl.startsWith("http") -> {
                Box(Modifier.size(32.dp).clip(CircleShape)) {
                    PosterImage(avatarUrl, Modifier.size(32.dp))
                }
            }
            profile != null -> {
                val initial =
                    profile.name
                        .trim()
                        .firstOrNull()
                        ?.uppercase() ?: "?"
                Text(
                    text = initial,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.textPrimary,
                )
            }
            else ->
                Icon(
                    Icons.Outlined.Person,
                    contentDescription = "Profile",
                    tint = AppColors.textSecondary,
                    modifier = Modifier.size(18.dp),
                )
        }
    }
}

/** Parses `#RRGGBB` / `RRGGBB`; null when malformed. */
private fun parseHex(value: String): Color? {
    val hex = value.removePrefix("#").trim()
    if (hex.length != 6) return null
    val parsed = hex.toLongOrNull(16) ?: return null
    return Color(0xFF000000L or parsed)
}
