package com.misgus.agustream.ui.streams

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.misgus.agustream.core.model.Stream
import com.misgus.agustream.ui.components.PosterImage
import com.misgus.agustream.ui.services.LocalAppServices
import com.misgus.agustream.ui.shell.RouteViewModelStoreOwner
import com.misgus.agustream.ui.theme.AppColors
import com.misgus.agustream.ui.theme.AppRadii
import com.misgus.agustream.ui.theme.AppScrim
import com.misgus.agustream.ui.theme.AppSpacing

/**
 * Modal that lists the available streams.
 *
 * The stream text is shown as-is: addons put their configured, multi-line format
 * in `name` and `description`, so the UI just renders it.
 */
@Composable
fun StreamsDialog(
    request: StreamsRequest,
    onDismiss: () -> Unit,
    onSelect: (Stream) -> Unit,
) {
    val services = LocalAppServices.current
    val owner = remember(request) { RouteViewModelStoreOwner() }
    DisposableEffect(owner) { onDispose { owner.viewModelStore.clear() } }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        CompositionLocalProvider(LocalViewModelStoreOwner provides owner) {
            val viewModel: StreamsViewModel =
                viewModel(key = request.id) {
                    StreamsViewModel(services.streams, request.type, request.id)
                }
            LaunchedEffect(request) { viewModel.load() }
            val state by viewModel.state.collectAsStateWithLifecycle()

            Box(
                modifier =
                    Modifier
                        .width(900.dp)
                        .fillMaxHeight(0.82f)
                        .clip(RoundedCornerShape(AppRadii.lg))
                        .background(AppColors.background),
            ) {
                Column(Modifier.fillMaxSize()) {
                    Header(
                        request = request,
                        subtitle = subtitle(state),
                        onRefresh = { viewModel.load(force = true) },
                        onClose = onDismiss,
                    )
                    OutlinedTextField(
                        value = state.query,
                        onValueChange = viewModel::setQuery,
                        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                        placeholder = { Text("Filter versions") },
                        singleLine = true,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = AppSpacing.xl, vertical = AppSpacing.md),
                    )
                    Content(state = state, onPlay = onSelect)
                }
            }
        }
    }
}

@Composable
private fun Header(
    request: StreamsRequest,
    subtitle: String,
    onRefresh: () -> Unit,
    onClose: () -> Unit,
) {
    Box(Modifier.fillMaxWidth().height(200.dp)) {
        PosterImage(request.background, Modifier.fillMaxSize())
        Box(Modifier.fillMaxSize().background(AppScrim.dialogHeader))
        Row(
            modifier = Modifier.align(Alignment.TopEnd).padding(AppSpacing.xs),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            IconButton(onClick = onRefresh) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }
        Column(
            modifier =
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(AppSpacing.xl),
        ) {
            Text(
                request.title,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.headlineSmall,
            )
            request.year?.takeIf { it.isNotEmpty() }?.let {
                Text(it, style = MaterialTheme.typography.bodyLarge)
            }
            Text(subtitle, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun Content(
    state: StreamsUiState,
    onPlay: (Stream) -> Unit,
) {
    when {
        state.isLoading && !state.hasLoaded ->
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator()
            }
        state.error != null && !state.hasLoaded ->
            Message(
                icon = Icons.Outlined.ErrorOutline,
                text = "Could not load sources from your addons.",
            )
        state.entries.isEmpty() ->
            Message(
                icon = Icons.Outlined.SearchOff,
                text =
                    if (state.groups.isEmpty()) {
                        "No sources found for this title."
                    } else {
                        "No versions match the filter."
                    },
            )
        else ->
            LazyColumn(Modifier.fillMaxSize()) {
                items(state.entries) { entry ->
                    StreamCard(
                        stream = entry.stream,
                        addonName = entry.addonName,
                        onPlay = { onPlay(entry.stream) },
                    )
                }
            }
    }
}

@Composable
private fun StreamCard(
    stream: Stream,
    addonName: String,
    onPlay: () -> Unit,
) {
    val clipboard = LocalClipboardManager.current
    val primary = listOf(stream.name, stream.title).firstOrNull { !it.isNullOrEmpty() } ?: addonName
    val secondary =
        listOf(stream.description, stream.title)
            .firstOrNull { !it.isNullOrEmpty() && it != primary }
    val size = formatSize(stream.behaviorHints?.videoSize)

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.xl, vertical = AppSpacing.xs)
                .clip(RoundedCornerShape(AppRadii.md))
                .background(AppColors.surface)
                .padding(AppSpacing.md),
        verticalAlignment = Alignment.Top,
    ) {
        IconButton(
            onClick = onPlay,
            modifier = Modifier.size(40.dp),
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = "Play")
        }
        Spacer(Modifier.width(AppSpacing.md))
        Column(Modifier.weight(1f)) {
            Text(
                primary,
                style = MaterialTheme.typography.titleMedium,
            )
            secondary?.let {
                Spacer(Modifier.height(2.dp))
                Text(it, style = MaterialTheme.typography.bodyMedium, color = AppColors.textSecondary)
            }
        }
        size?.let {
            Spacer(Modifier.width(AppSpacing.sm))
            Text(it, style = MaterialTheme.typography.bodyMedium)
        }
        IconButton(onClick = {
            (stream.url ?: stream.externalUrl)?.let { clipboard.setText(AnnotatedString(it)) }
        }) {
            Icon(Icons.Outlined.ContentCopy, contentDescription = "Copy link")
        }
    }
}

@Composable
private fun Message(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
) {
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = AppColors.textSecondary, modifier = Modifier.size(40.dp))
            Spacer(Modifier.height(AppSpacing.md))
            Text(text, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

private fun subtitle(state: StreamsUiState): String =
    when {
        state.isLoading && !state.hasLoaded -> "Loading…"
        state.error != null && !state.hasLoaded -> "Could not load sources"
        state.totalCount == 1 -> "1 version"
        else -> "${state.totalCount} versions"
    }

private fun formatSize(bytes: Long?): String? {
    if (bytes == null || bytes <= 0) return null
    val units = listOf("B", "KB", "MB", "GB", "TB")
    var value = bytes.toDouble()
    var unit = 0
    while (value >= 1024 && unit < units.lastIndex) {
        value /= 1024
        unit++
    }
    val decimals = if (value >= 10 || unit == 0) 0 else 1
    return "${round(value, decimals)} ${units[unit]}"
}

private fun round(
    value: Double,
    decimals: Int,
): String {
    var factor = 1.0
    repeat(decimals) { factor *= 10 }
    val scaled = kotlin.math.round(value * factor) / factor
    return if (decimals == 0) scaled.toLong().toString() else scaled.toString()
}
