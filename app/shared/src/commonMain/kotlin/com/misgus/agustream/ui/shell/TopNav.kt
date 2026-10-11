package com.misgus.agustream.ui.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.misgus.agustream.core.model.BackendProfile
import com.misgus.agustream.ui.components.ProfileAvatar
import com.misgus.agustream.ui.theme.AppColors
import com.misgus.agustream.ui.theme.AppRadii
import com.misgus.agustream.ui.theme.AppSizes
import com.misgus.agustream.ui.theme.AppSpacing

/** Horizontal top navigation, with the profile and section items centered. */
@Composable
fun TopNav(
    selected: AppSection,
    onSelected: (AppSection) -> Unit,
    activeProfile: BackendProfile? = null,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(AppSizes.topBarHeight)
                .padding(horizontal = AppSpacing.xl),
        horizontalArrangement = Arrangement.Center,
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

@Composable
private fun NavItem(
    section: AppSection,
    selected: Boolean,
    onTap: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .padding(horizontal = AppSpacing.xs)
                .clip(RoundedCornerShape(AppRadii.md))
                .background(if (selected) AppColors.surfaceHigh else Color.Transparent)
                .clickable(onClick = onTap)
                .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (selected) section.selectedIcon else section.icon,
            contentDescription = section.label,
            tint = if (selected) AppColors.textPrimary else AppColors.textSecondary,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(AppSpacing.sm))
        Text(
            text = section.label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) AppColors.textPrimary else AppColors.textSecondary,
        )
    }
}
