package com.misgus.agustream.ui.onboarding

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.misgus.agustream.core.repository.SignInMethod
import com.misgus.agustream.ui.services.LocalAppServices
import com.misgus.agustream.ui.theme.AppColors
import com.misgus.agustream.ui.theme.AppSpacing
import com.misgus.agustream.ui.theme.AppType
import com.misgus.agustream.ui.theme.LocalWindowSize
import com.misgus.agustream.ui.theme.screenPadding

/** Max width of the onboarding column, so it stays readable on a wide window. */
private val ONBOARDING_MAX_WIDTH = 420.dp

/**
 * First-run screen: what the app is, and how to sign in.
 *
 * Shown before the shell while there is no session, so a new user does not have
 * to find the sign-in card inside Settings. The form it renders depends on what
 * the backend offers through `signInMethods`.
 */
@Composable
fun OnboardingScreen(modifier: Modifier = Modifier) {
    val account = LocalAppServices.current.account

    Box(modifier.fillMaxSize().safeDrawingPadding(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.widthIn(max = ONBOARDING_MAX_WIDTH).padding(LocalWindowSize.current.screenPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Agustream", style = AppType.screenTitle())
            Spacer(Modifier.height(AppSpacing.sm))
            Text(
                "Your library, addons and watch progress, together in one place.",
                style = MaterialTheme.typography.bodyLarge,
                color = AppColors.textSecondary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(AppSpacing.xl))
            if (SignInMethod.EmailPassword in account.signInMethods) {
                SignInForm()
            } else {
                Text(
                    "This backend does not support signing in from the app.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
