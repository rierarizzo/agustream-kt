package com.misgus.agustream.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.misgus.agustream.core.player.PlaybackTarget
import com.misgus.agustream.core.repository.PosterSize
import com.misgus.agustream.core.repository.SignInMethod
import com.misgus.agustream.core.repository.SignInRequest
import com.misgus.agustream.ui.components.PillButton
import com.misgus.agustream.ui.components.toUserMessage
import com.misgus.agustream.ui.platform.openInDefaultApp
import com.misgus.agustream.ui.platform.pickVideoFile
import com.misgus.agustream.ui.services.LocalAppServices
import com.misgus.agustream.ui.theme.AppColors
import com.misgus.agustream.ui.theme.AppRadii
import com.misgus.agustream.ui.theme.AppSpacing
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/**
 * Settings section.
 *
 * Holds two temporary cards: one to sign in (the real login screen is not built
 * yet) and the phase 1 playback test.
 */
@Composable
fun SettingsScreen(onOpenPlayer: (PlaybackTarget) -> Unit) {
    val settings = LocalAppServices.current.settings
    val state by settings.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var error by remember { mutableStateOf<String?>(null) }
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
            modifier = Modifier.widthIn(max = SETTINGS_MAX_WIDTH).fillMaxWidth().padding(AppSpacing.xl),
        ) {
            Text("Settings", style = MaterialTheme.typography.headlineLarge)
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
                            error = "Could not open the file with the default app."
                        }
                    }
                },
                onReload = { save { settings.reload() } },
            )
            error?.let {
                Spacer(Modifier.height(AppSpacing.sm))
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
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

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    SettingsCard(
        title = "Account",
        subtitle =
            if (state.isSignedIn) {
                "Signed in as ${state.email ?: "your account"}."
            } else {
                "Sign in to load your library."
            },
    ) {
        if (state.isSignedIn) {
            PillButton(
                text = "Sign out",
                onClick = { scope.launch { account.signOut() } },
                icon = Icons.AutoMirrored.Outlined.Logout,
                primary = false,
                enabled = !busy,
            )
        } else if (SignInMethod.EmailPassword in account.signInMethods) {
            Column {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(AppSpacing.sm))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                )
                error?.let {
                    Spacer(Modifier.height(AppSpacing.sm))
                    Text(
                        it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                Spacer(Modifier.height(AppSpacing.md))
                PillButton(
                    text = "Sign in",
                    onClick = {
                        busy = true
                        error = null
                        scope.launch {
                            try {
                                account.signIn(SignInRequest.EmailPassword(email.trim(), password))
                                password = ""
                            } catch (e: CancellationException) {
                                throw e
                            } catch (e: Throwable) {
                                error = e.toUserMessage()
                            } finally {
                                busy = false
                            }
                        }
                    },
                    icon = Icons.AutoMirrored.Outlined.Login,
                    enabled = email.isNotBlank() && password.isNotEmpty(),
                    loading = busy,
                )
            }
        } else {
            Text(
                "This account signs in through another method.",
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
    SettingsCard(
        title = "Appearance",
        subtitle = "How the app looks and moves.",
    ) {
        Column {
            SettingsToggle(
                title = "Animations",
                subtitle = "Transitions, hover effects and loading skeletons.",
                checked = animationsEnabled,
                onCheckedChange = onAnimationsChange,
            )
            SettingsChoice(
                title = "Poster size",
                subtitle = "Catalogs, Discover and \"More like this\".",
                selected = posterSize,
                onSelect = onPosterSizeChange,
            )
            SettingsChoice(
                title = "Library poster size",
                subtitle = "The library grid only.",
                selected = libraryPosterSize,
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
        title = "Playback",
        subtitle = "How titles play.",
    ) {
        Column {
            SettingsToggle(
                title = "Resume playback",
                subtitle = "Continue a title from where you left it.",
                checked = resumePlayback,
                onCheckedChange = onResumeChange,
            )
            SettingsToggle(
                title = "Auto-play next episode",
                subtitle = "Start the next episode when one finishes.",
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
        title = "Configuration file",
        subtitle = "Edit it by hand, then reload.",
    ) {
        Column {
            Text(
                filePath,
                style = MaterialTheme.typography.labelSmall,
                color = AppColors.textSecondary,
            )
            Spacer(Modifier.height(AppSpacing.sm))
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                PillButton(text = "Open settings.json", onClick = onOpen, primary = false)
                PillButton(text = "Reload from file", onClick = onReload, primary = false)
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
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = AppColors.textSecondary)
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
        title = "Open a video (temporary)",
        subtitle = "Playback test kept from phase 1.",
    ) {
        Column {
            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text("Video URL") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(AppSpacing.sm))
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                PillButton(
                    text = "Play URL",
                    onClick = { onOpenPlayer(PlaybackTarget.raw(url.trim())) },
                    icon = Icons.Default.PlayArrow,
                    enabled = url.isNotBlank(),
                )
                PillButton(
                    text = "Open local file",
                    onClick = {
                        scope.launch {
                            pickVideoFile()?.let { onOpenPlayer(PlaybackTarget.raw(it)) }
                        }
                    },
                    icon = Icons.Outlined.FolderOpen,
                    primary = false,
                )
            }
        }
    }
}

@Composable
private fun SettingsChoice(
    title: String,
    subtitle: String,
    selected: PosterSize,
    onSelect: (PosterSize) -> Unit,
) {
    Column(Modifier.fillMaxWidth().padding(vertical = AppSpacing.sm)) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = AppColors.textSecondary)
        Spacer(Modifier.height(AppSpacing.sm))
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            PosterSize.entries.forEach { option ->
                FilterChip(
                    selected = option == selected,
                    onClick = { onSelect(option) },
                    label = { Text(option.label()) },
                )
            }
        }
    }
}

private fun PosterSize.label(): String =
    when (this) {
        PosterSize.SMALL -> "Small"
        PosterSize.DEFAULT -> "Default"
        PosterSize.LARGE -> "Large"
    }

@Composable
private fun SettingsCard(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(AppRadii.lg))
                .background(AppColors.surface)
                .padding(AppSpacing.lg),
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(AppSpacing.xs))
        Text(subtitle, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(AppSpacing.md))
        content()
    }
}

/** Readable width: the label and the toggle stay close instead of a screen apart. */
private val SETTINGS_MAX_WIDTH = 720.dp
