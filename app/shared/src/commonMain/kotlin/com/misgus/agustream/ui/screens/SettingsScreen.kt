package com.misgus.agustream.ui.screens

import agustream.app.shared.generated.resources.Res
import agustream.app.shared.generated.resources.action_sign_out
import agustream.app.shared.generated.resources.error_open_file
import agustream.app.shared.generated.resources.poster_size_default
import agustream.app.shared.generated.resources.poster_size_large
import agustream.app.shared.generated.resources.poster_size_small
import agustream.app.shared.generated.resources.settings_account
import agustream.app.shared.generated.resources.settings_animations
import agustream.app.shared.generated.resources.settings_animations_subtitle
import agustream.app.shared.generated.resources.settings_appearance
import agustream.app.shared.generated.resources.settings_appearance_subtitle
import agustream.app.shared.generated.resources.settings_autoplay
import agustream.app.shared.generated.resources.settings_autoplay_subtitle
import agustream.app.shared.generated.resources.settings_config_file
import agustream.app.shared.generated.resources.settings_config_file_subtitle
import agustream.app.shared.generated.resources.settings_library_poster_size
import agustream.app.shared.generated.resources.settings_library_poster_size_subtitle
import agustream.app.shared.generated.resources.settings_open_file
import agustream.app.shared.generated.resources.settings_open_local
import agustream.app.shared.generated.resources.settings_open_video
import agustream.app.shared.generated.resources.settings_open_video_subtitle
import agustream.app.shared.generated.resources.settings_play_url
import agustream.app.shared.generated.resources.settings_playback
import agustream.app.shared.generated.resources.settings_playback_subtitle
import agustream.app.shared.generated.resources.settings_poster_size
import agustream.app.shared.generated.resources.settings_poster_size_subtitle
import agustream.app.shared.generated.resources.settings_reload
import agustream.app.shared.generated.resources.settings_resume
import agustream.app.shared.generated.resources.settings_resume_subtitle
import agustream.app.shared.generated.resources.settings_sign_in_other_method
import agustream.app.shared.generated.resources.settings_sign_in_to_load
import agustream.app.shared.generated.resources.settings_signed_in_account
import agustream.app.shared.generated.resources.settings_signed_in_as
import agustream.app.shared.generated.resources.settings_title
import agustream.app.shared.generated.resources.settings_video_url
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.misgus.agustream.core.player.PlaybackTarget
import com.misgus.agustream.core.repository.PosterSize
import com.misgus.agustream.core.repository.SignInMethod
import com.misgus.agustream.ui.components.ActionButton
import com.misgus.agustream.ui.components.ActionButtonStyle
import com.misgus.agustream.ui.components.AppTextField
import com.misgus.agustream.ui.components.sectionHeading
import com.misgus.agustream.ui.components.toUserMessage
import com.misgus.agustream.ui.onboarding.SignInForm
import com.misgus.agustream.ui.platform.openInDefaultApp
import com.misgus.agustream.ui.platform.pickVideoFile
import com.misgus.agustream.ui.services.LocalAppServices
import com.misgus.agustream.ui.theme.AppSizes
import com.misgus.agustream.ui.theme.AppSpacing
import com.misgus.agustream.ui.theme.AppType
import com.misgus.agustream.ui.theme.LocalWindowSize
import com.misgus.agustream.ui.theme.screenPadding
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Settings section.
 *
 * Groups the local preferences (appearance, playback) and the account card. The
 * sign-in form is shared with the onboarding screen.
 */
@Composable
fun SettingsScreen(onOpenPlayer: (PlaybackTarget) -> Unit) {
    val settings = LocalAppServices.current.settings
    val state by settings.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var error by remember { mutableStateOf<StringResource?>(null) }
    val save: (suspend () -> Unit) -> Unit = { action ->
        scope.launch {
            runCatching { action() }.onFailure { error = it.toUserMessage() }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // A readable column instead of a card stretched across a wide window.
        Column(
            modifier = Modifier.widthIn(max = AppSizes.contentMaxWidth).fillMaxWidth().padding(LocalWindowSize.current.screenPadding),
        ) {
            Text(
                stringResource(Res.string.settings_title),
                style = AppType.screenTitle(),
                modifier = Modifier.sectionHeading(),
            )
            Spacer(Modifier.height(AppSpacing.lg))
            AccountCard()
            Spacer(Modifier.height(AppSpacing.lg))
            AppearanceCard(
                animationsEnabled = state.animationsEnabled,
                onAnimationsChange = { value -> save { settings.setAnimationsEnabled(value) } },
                posterSize = state.posterSize,
                onPosterSizeChange = { value -> save { settings.setPosterSize(value) } },
                libraryPosterSize = state.libraryPosterSize,
                onLibraryPosterSizeChange = { value -> save { settings.setLibraryPosterSize(value) } },
            )
            Spacer(Modifier.height(AppSpacing.lg))
            PlaybackSettingsCard(
                resumePlayback = state.resumePlayback,
                autoPlayNext = state.autoPlayNext,
                onResumeChange = { value -> save { settings.setResumePlayback(value) } },
                onAutoPlayChange = { value -> save { settings.setAutoPlayNext(value) } },
            )
            Spacer(Modifier.height(AppSpacing.lg))
            ConfigFileCard(
                filePath = settings.filePath,
                onOpen = {
                    save {
                        if (!openInDefaultApp(settings.filePath)) {
                            error = Res.string.error_open_file
                        }
                    }
                },
                onReload = { save { settings.reload() } },
            )
            error?.let {
                Spacer(Modifier.height(AppSpacing.sm))
                Text(stringResource(it), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(AppSpacing.lg))
            PlaybackCard(onOpenPlayer)
        }
    }
}

@Composable
private fun AccountCard() {
    val account = LocalAppServices.current.account
    val state by account.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var signingOut by remember { mutableStateOf(false) }

    val unknownAccount = stringResource(Res.string.settings_signed_in_account)
    SettingsCard(
        title = stringResource(Res.string.settings_account),
        subtitle =
            if (state.isSignedIn) {
                stringResource(Res.string.settings_signed_in_as, state.email ?: unknownAccount)
            } else {
                stringResource(Res.string.settings_sign_in_to_load)
            },
    ) {
        if (state.isSignedIn) {
            ActionButton(
                text = stringResource(Res.string.action_sign_out),
                onClick = {
                    signingOut = true
                    scope.launch {
                        try {
                            account.signOut()
                        } finally {
                            signingOut = false
                        }
                    }
                },
                icon = Icons.AutoMirrored.Outlined.Logout,
                style = ActionButtonStyle.Tonal,
                enabled = !signingOut,
                loading = signingOut,
            )
        } else if (SignInMethod.EmailPassword in account.signInMethods) {
            SignInForm()
        } else {
            Text(
                stringResource(Res.string.settings_sign_in_other_method),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun AppearanceCard(
    animationsEnabled: Boolean,
    onAnimationsChange: (Boolean) -> Unit,
    posterSize: PosterSize,
    onPosterSizeChange: (PosterSize) -> Unit,
    libraryPosterSize: PosterSize,
    onLibraryPosterSizeChange: (PosterSize) -> Unit,
) {
    val posterSizeLabels =
        mapOf(
            PosterSize.SMALL to stringResource(Res.string.poster_size_small),
            PosterSize.DEFAULT to stringResource(Res.string.poster_size_default),
            PosterSize.LARGE to stringResource(Res.string.poster_size_large),
        )
    SettingsCard(
        title = stringResource(Res.string.settings_appearance),
        subtitle = stringResource(Res.string.settings_appearance_subtitle),
    ) {
        Column {
            SettingsToggle(
                title = stringResource(Res.string.settings_animations),
                subtitle = stringResource(Res.string.settings_animations_subtitle),
                checked = animationsEnabled,
                onCheckedChange = onAnimationsChange,
            )
            SettingsChoice(
                title = stringResource(Res.string.settings_poster_size),
                subtitle = stringResource(Res.string.settings_poster_size_subtitle),
                options = PosterSize.entries,
                selected = posterSize,
                label = { posterSizeLabels.getValue(it) },
                onSelect = onPosterSizeChange,
            )
            SettingsChoice(
                title = stringResource(Res.string.settings_library_poster_size),
                subtitle = stringResource(Res.string.settings_library_poster_size_subtitle),
                options = PosterSize.entries,
                selected = libraryPosterSize,
                label = { posterSizeLabels.getValue(it) },
                onSelect = onLibraryPosterSizeChange,
            )
        }
    }
}

@Composable
private fun PlaybackSettingsCard(
    resumePlayback: Boolean,
    autoPlayNext: Boolean,
    onResumeChange: (Boolean) -> Unit,
    onAutoPlayChange: (Boolean) -> Unit,
) {
    SettingsCard(
        title = stringResource(Res.string.settings_playback),
        subtitle = stringResource(Res.string.settings_playback_subtitle),
    ) {
        Column {
            SettingsToggle(
                title = stringResource(Res.string.settings_resume),
                subtitle = stringResource(Res.string.settings_resume_subtitle),
                checked = resumePlayback,
                onCheckedChange = onResumeChange,
            )
            SettingsToggle(
                title = stringResource(Res.string.settings_autoplay),
                subtitle = stringResource(Res.string.settings_autoplay_subtitle),
                checked = autoPlayNext,
                onCheckedChange = onAutoPlayChange,
            )
        }
    }
}

@Composable
private fun ConfigFileCard(
    filePath: String,
    onOpen: () -> Unit,
    onReload: () -> Unit,
) {
    SettingsCard(
        title = stringResource(Res.string.settings_config_file),
        subtitle = stringResource(Res.string.settings_config_file_subtitle),
    ) {
        Column {
            Text(
                filePath,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(AppSpacing.sm))
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                ActionButton(
                    text = stringResource(Res.string.settings_open_file),
                    onClick = onOpen,
                    style = ActionButtonStyle.Tonal,
                )
                ActionButton(
                    text = stringResource(Res.string.settings_reload),
                    onClick = onReload,
                    style = ActionButtonStyle.Tonal,
                )
            }
        }
    }
}

@Composable
private fun SettingsToggle(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.width(AppSpacing.md))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun PlaybackCard(onOpenPlayer: (PlaybackTarget) -> Unit) {
    var url by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    SettingsCard(
        title = stringResource(Res.string.settings_open_video),
        subtitle = stringResource(Res.string.settings_open_video_subtitle),
    ) {
        Column {
            AppTextField(
                value = url,
                onValueChange = { url = it },
                label = stringResource(Res.string.settings_video_url),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(AppSpacing.sm))
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                ActionButton(
                    text = stringResource(Res.string.settings_play_url),
                    onClick = { onOpenPlayer(PlaybackTarget.raw(url.trim())) },
                    icon = Icons.Default.PlayArrow,
                    enabled = url.isNotBlank(),
                )
                ActionButton(
                    text = stringResource(Res.string.settings_open_local),
                    onClick = {
                        scope.launch {
                            pickVideoFile()?.let { onOpenPlayer(PlaybackTarget.raw(it)) }
                        }
                    },
                    icon = Icons.Outlined.FolderOpen,
                    style = ActionButtonStyle.Tonal,
                )
            }
        }
    }
}

@Composable
private fun <T> SettingsChoice(
    title: String,
    subtitle: String,
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit,
) {
    Column(Modifier.fillMaxWidth().padding(vertical = AppSpacing.sm)) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(AppSpacing.sm))
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            options.forEach { option ->
                FilterChip(
                    selected = option == selected,
                    onClick = { onSelect(option) },
                    label = { Text(label(option)) },
                )
            }
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(AppSpacing.lg)) {
            Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.sectionHeading())
            Spacer(Modifier.height(AppSpacing.xs))
            Text(subtitle, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(AppSpacing.md))
            content()
        }
    }
}
