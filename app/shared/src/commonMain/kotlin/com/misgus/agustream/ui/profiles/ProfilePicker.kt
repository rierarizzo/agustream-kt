package com.misgus.agustream.ui.profiles

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.misgus.agustream.core.model.BackendProfile
import com.misgus.agustream.ui.components.PosterImage
import com.misgus.agustream.ui.services.LocalAppServices
import com.misgus.agustream.ui.theme.AppColors
import com.misgus.agustream.ui.theme.AppRadii
import com.misgus.agustream.ui.theme.AppSpacing
import kotlinx.coroutines.launch

/**
 * Asks which profile to work with.
 *
 * Shown as a gate: the library, the watch progress and every write belong to a
 * profile, so there is no safe default. Choosing one is mandatory.
 */
@Composable
fun ProfilePicker(modifier: Modifier = Modifier) {
    val account = LocalAppServices.current.account
    val state by account.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.width(720.dp).padding(AppSpacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Who is watching?", style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(AppSpacing.xs))
            Text(
                "The library and the watch progress belong to a profile.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(AppSpacing.xl))

            val isFirstLoad = state.isLoadingProfiles && state.profiles.isEmpty()
            when {
                isFirstLoad -> CircularProgressIndicator()
                state.profilesError != null -> Problem(
                    message = state.profilesError.toString(),
                    onRetry = { scope.launch { account.loadProfiles() } },
                )
                state.profiles.isEmpty() -> Problem(
                    message = "This account has no profiles.",
                    onRetry = { scope.launch { account.loadProfiles() } },
                )
                else -> androidx.compose.foundation.layout.FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.lg, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
                ) {
                    state.profiles.forEach { profile ->
                        ProfileTile(profile) { scope.launch { account.selectProfile(profile) } }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileTile(profile: BackendProfile, onTap: () -> Unit) {
    Column(
        modifier = Modifier.clickable(onClick = onTap).padding(AppSpacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(102.dp)
                .clip(CircleShape)
                .border(2.dp, Color.Transparent, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            ProfileAvatar(profile)
        }
        Spacer(Modifier.height(AppSpacing.sm))
        Text(profile.name, style = MaterialTheme.typography.bodyLarge)
        if (profile.pinEnabled) {
            Text("PIN", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ProfileAvatar(profile: BackendProfile) {
    val color = profile.avatarColorHex?.let(::parseHex) ?: AppColors.surfaceHigh
    Box(
        modifier = Modifier.size(96.dp).clip(CircleShape).background(color),
        contentAlignment = Alignment.Center,
    ) {
        val avatarUrl = profile.avatarUrl
        if (!avatarUrl.isNullOrEmpty() && avatarUrl.startsWith("http")) {
            PosterImage(avatarUrl, Modifier.size(96.dp))
        } else {
            Text(
                text = profile.name.trim().firstOrNull()?.uppercase() ?: "?",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun Problem(message: String, onRetry: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            Icons.Outlined.ErrorOutline,
            contentDescription = null,
            tint = AppColors.textSecondary,
            modifier = Modifier.size(36.dp),
        )
        Spacer(Modifier.height(AppSpacing.md))
        Text(message, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(AppSpacing.md))
        OutlinedButton(onClick = onRetry) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(AppSpacing.sm))
            Text("Try again")
        }
    }
}

private fun parseHex(value: String): Color? {
    val hex = value.removePrefix("#").trim()
    if (hex.length != 6) return null
    val parsed = hex.toLongOrNull(16) ?: return null
    return Color(0xFF000000L or parsed)
}
