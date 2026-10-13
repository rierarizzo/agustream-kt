package com.misgus.agustream.ui.profiles

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.misgus.agustream.core.model.BackendProfile
import com.misgus.agustream.ui.components.ActionButton
import com.misgus.agustream.ui.components.ProfileAvatar
import com.misgus.agustream.ui.components.clickableSurface
import com.misgus.agustream.ui.components.interactionHighlight
import com.misgus.agustream.ui.components.rememberInteraction
import com.misgus.agustream.ui.services.LocalAppServices
import com.misgus.agustream.ui.theme.AppSpacing
import com.misgus.agustream.ui.theme.AppType
import com.misgus.agustream.ui.theme.LocalWindowSize
import com.misgus.agustream.ui.theme.screenPadding
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

    Box(modifier.fillMaxSize().safeDrawingPadding(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.width(720.dp).padding(LocalWindowSize.current.screenPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Who is watching?", style = AppType.screenTitle())
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
                state.profilesError != null ->
                    Problem(
                        message = state.profilesError.toString(),
                        onRetry = { scope.launch { account.loadProfiles() } },
                    )
                state.profiles.isEmpty() ->
                    Problem(
                        message = "This account has no profiles.",
                        onRetry = { scope.launch { account.loadProfiles() } },
                    )
                else ->
                    androidx.compose.foundation.layout.FlowRow(
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
private fun ProfileTile(
    profile: BackendProfile,
    onTap: () -> Unit,
) {
    val interaction = rememberInteraction()
    Column(
        modifier =
            Modifier
                .clip(MaterialTheme.shapes.small)
                .background(interactionHighlight(interaction))
                .clickableSurface(interaction, onTap)
                .padding(AppSpacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier =
                Modifier
                    .size(102.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.Transparent, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            ProfileAvatar(
                profile = profile,
                size = 96.dp,
                initialStyle = MaterialTheme.typography.headlineLarge,
            )
        }
        Spacer(Modifier.height(AppSpacing.sm))
        Text(profile.name, style = MaterialTheme.typography.bodyLarge)
        if (profile.pinEnabled) {
            Text("PIN", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun Problem(
    message: String,
    onRetry: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            Icons.Outlined.ErrorOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(36.dp),
        )
        Spacer(Modifier.height(AppSpacing.md))
        Text(message, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(AppSpacing.md))
        ActionButton(
            text = "Try again",
            onClick = onRetry,
            icon = Icons.Default.Refresh,
            primary = false,
        )
    }
}
