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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Login
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import com.misgus.agustream.core.BackendException
import com.misgus.agustream.core.player.PlaybackTarget
import com.misgus.agustream.ui.platform.pickVideoFile
import com.misgus.agustream.ui.services.LocalAppServices
import com.misgus.agustream.ui.theme.AppColors
import com.misgus.agustream.ui.theme.AppRadii
import com.misgus.agustream.ui.theme.AppSpacing
import kotlinx.coroutines.launch

/**
 * Settings section.
 *
 * Holds two temporary cards: one to sign in (the real login screen is not built
 * yet) and the phase 1 playback test.
 */
@Composable
fun SettingsScreen(onOpenPlayer: (PlaybackTarget) -> Unit) {
    val account = LocalAppServices.current.account
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(AppSpacing.xl),
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(AppSpacing.lg))
        AccountCard()
        Spacer(Modifier.height(AppSpacing.lg))
        PlaybackCard(onOpenPlayer)
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
        subtitle = if (state.isSignedIn) {
            "Signed in as ${state.email ?: "your account"}."
        } else {
            "Sign in to load your library."
        },
    ) {
        if (state.isSignedIn) {
            OutlinedButton(
                onClick = { scope.launch { account.signOut() } },
                enabled = !busy,
            ) {
                Icon(Icons.Outlined.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(AppSpacing.sm))
                Text("Sign out")
            }
        } else {
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
                FilledTonalButton(
                    enabled = !busy && email.isNotBlank() && password.isNotEmpty(),
                    onClick = {
                        busy = true
                        error = null
                        scope.launch {
                            try {
                                account.signIn(email.trim(), password)
                                password = ""
                            } catch (e: BackendException) {
                                error = e.message
                            } catch (e: Throwable) {
                                error = e.toString()
                            } finally {
                                busy = false
                            }
                        }
                    },
                ) {
                    if (busy) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Outlined.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(AppSpacing.sm))
                    Text("Sign in")
                }
            }
        }
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
                FilledTonalButton(
                    enabled = url.isNotBlank(),
                    onClick = { onOpenPlayer(PlaybackTarget.raw(url.trim())) },
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(AppSpacing.sm))
                    Text("Play URL")
                }
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            pickVideoFile()?.let { onOpenPlayer(PlaybackTarget.raw(it)) }
                        }
                    },
                ) {
                    Icon(Icons.Outlined.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(AppSpacing.sm))
                    Text("Open local file")
                }
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
    Column(
        modifier = Modifier
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
