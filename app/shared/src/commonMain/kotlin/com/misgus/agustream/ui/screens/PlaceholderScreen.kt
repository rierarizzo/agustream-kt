package com.misgus.agustream.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.misgus.agustream.ui.shell.AppSection
import com.misgus.agustream.ui.theme.AppColors
import com.misgus.agustream.ui.theme.AppSpacing

/** Stand-in for a section that has not been built yet. */
@Composable
fun PlaceholderScreen(section: AppSection) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = section.icon,
                contentDescription = null,
                tint = AppColors.textSecondary,
                modifier = Modifier.size(44.dp),
            )
            Spacer(Modifier.height(AppSpacing.md))
            Text(section.label, style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(AppSpacing.sm))
            Text("Coming in a later part", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
