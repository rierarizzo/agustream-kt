package com.misgus.agustream.ui.onboarding

import agustream.app.shared.generated.resources.Res
import agustream.app.shared.generated.resources.action_sign_in
import agustream.app.shared.generated.resources.field_email
import agustream.app.shared.generated.resources.field_password
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.misgus.agustream.core.repository.SignInRequest
import com.misgus.agustream.ui.components.ActionButton
import com.misgus.agustream.ui.components.toUserMessage
import com.misgus.agustream.ui.services.LocalAppServices
import com.misgus.agustream.ui.theme.AppSpacing
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Email + password form, shared by the onboarding screen and the Settings card.
 *
 * It owns its input, its busy flag and its error text, and signs in through the
 * account repository; callers only decide where it appears.
 */
@Composable
fun SignInForm(modifier: Modifier = Modifier) {
    val account = LocalAppServices.current.account
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<StringResource?>(null) }

    val submit: () -> Unit = {
        if (!busy && email.isNotBlank() && password.isNotEmpty()) {
            busy = true
            error = null
            scope.launch {
                try {
                    account.signIn(SignInRequest.EmailPassword(email.trim(), password))
                    password = ""
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (failure: Throwable) {
                    error = failure.toUserMessage()
                } finally {
                    busy = false
                }
            }
        }
    }

    Column(modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(Res.string.field_email)) },
            singleLine = true,
            enabled = !busy,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(AppSpacing.sm))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(Res.string.field_password)) },
            singleLine = true,
            enabled = !busy,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { submit() }),
            modifier = Modifier.fillMaxWidth(),
        )
        error?.let {
            Spacer(Modifier.height(AppSpacing.sm))
            Text(
                stringResource(it),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }
        Spacer(Modifier.height(AppSpacing.md))
        ActionButton(
            text = stringResource(Res.string.action_sign_in),
            onClick = submit,
            icon = Icons.AutoMirrored.Outlined.Login,
            enabled = email.isNotBlank() && password.isNotEmpty(),
            loading = busy,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
