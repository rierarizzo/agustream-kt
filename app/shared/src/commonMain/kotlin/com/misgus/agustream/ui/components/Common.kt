package com.misgus.agustream.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.misgus.agustream.ui.theme.AppColors
import com.misgus.agustream.ui.theme.AppIconSize
import com.misgus.agustream.ui.theme.AppRadii
import com.misgus.agustream.ui.theme.AppSpacing

/** Check badge shown on a title the account marked as watched. */
@Composable
fun WatchedBadge(iconSize: Int = 14) {
    Box(
        modifier =
            Modifier
                .clip(CircleShape)
                .background(AppColors.mediaScrim)
                .padding(3.dp),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Watched",
            tint = AppColors.accent,
            modifier = Modifier.size(iconSize.dp),
        )
    }
}

/**
 * Small "Syncing…" pill shown while the watched badges are resolved in the
 * background, so the content can paint immediately.
 */
@Composable
fun SyncingIndicator() {
    Row(
        modifier =
            Modifier
                .clip(RoundedCornerShape(AppRadii.lg))
                .background(AppColors.surfaceHigh)
                .padding(horizontal = AppSpacing.sm + 2.dp, vertical = AppSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
        Spacer(Modifier.width(AppSpacing.sm))
        Text("Syncing…", style = MaterialTheme.typography.bodyMedium)
    }
}

/** Centered icon + title + message used for empty, loading and error states. */
@Composable
fun Message(
    icon: ImageVector,
    title: String,
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.width(360.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(icon, contentDescription = null, tint = AppColors.textSecondary, modifier = Modifier.size(40.dp))
            Spacer(Modifier.size(AppSpacing.md))
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.size(AppSpacing.xs))
            Text(
                message,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
            )
            if (actionLabel != null && onAction != null) {
                Spacer(Modifier.size(AppSpacing.md))
                PillButton(text = actionLabel, onClick = onAction)
            }
        }
    }
}

/**
 * Title row above a horizontal rail of posters, with an optional "See all".
 *
 * Shared by Home and Discover so the rails keep the same rhythm.
 */
@Composable
fun SectionHeader(
    title: String,
    onSeeAll: (() -> Unit)? = null,
) {
    Row(
        modifier =
            Modifier.fillMaxWidth().padding(
                start = AppSpacing.xl,
                end = AppSpacing.xl,
                top = AppSpacing.lg,
                bottom = AppSpacing.md,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
        onSeeAll?.let {
            TextButton(onClick = it) {
                Text("See all")
                Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(AppIconSize.sm))
            }
        }
    }
}

/** Artwork with a graceful placeholder while it loads or fails. */
@Composable
fun PosterImage(
    url: String?,
    modifier: Modifier = Modifier,
) {
    Box(modifier.background(AppColors.surface)) {
        if (url.isNullOrEmpty()) {
            PosterFallback()
        } else {
            AsyncImage(
                model = url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun PosterFallback() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Icon(
            imageVector = Icons.Outlined.Movie,
            contentDescription = null,
            tint = AppColors.textSecondary,
        )
    }
}
