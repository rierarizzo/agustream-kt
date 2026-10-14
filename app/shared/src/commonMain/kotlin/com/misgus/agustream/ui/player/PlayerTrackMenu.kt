package com.misgus.agustream.ui.player

import agustream.app.shared.generated.resources.Res
import agustream.app.shared.generated.resources.action_close
import agustream.app.shared.generated.resources.player_no_tracks
import agustream.app.shared.generated.resources.player_track_ai
import agustream.app.shared.generated.resources.player_track_subtitle
import agustream.app.shared.generated.resources.player_track_trusted
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.misgus.agustream.core.model.Subtitle
import com.misgus.agustream.core.player.MediaTrack
import com.misgus.agustream.ui.theme.AppSpacing
import org.jetbrains.compose.resources.stringResource

/** One selectable row of a track menu. */
internal data class TrackRow(
    val label: String,
    val detail: String? = null,
    val selected: Boolean,
    val onSelect: () -> Unit,
)

/** Audio/subtitle track picker shown by the player. */
@Composable
internal fun TrackMenu(
    title: String,
    rows: List<TrackRow>,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.action_close)) }
        },
        title = { Text(title) },
        text = {
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).selectableGroup()) {
                if (rows.isEmpty()) {
                    Text(stringResource(Res.string.player_no_tracks), style = MaterialTheme.typography.bodyMedium)
                }
                rows.forEach { row ->
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = row.selected,
                                    onClick = {
                                        row.onSelect()
                                        onDismiss()
                                    },
                                ).padding(vertical = AppSpacing.sm),
                    ) {
                        Text(
                            text = if (row.selected) "● ${row.label}" else "○ ${row.label}",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        row.detail?.let {
                            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        },
    )
}

internal fun trackLabel(track: MediaTrack): String =
    track.title?.takeIf { it.isNotEmpty() } ?: track.language?.takeIf { it.isNotEmpty() } ?: track.id

@Composable
internal fun subtitleSubtitleTitle(subtitle: Subtitle): String {
    val name = subtitle.languageName
    val code = subtitle.langCode ?: subtitle.lang
    return if (name != null) {
        if (code.isNullOrEmpty()) name else "$name ($code)"
    } else {
        subtitle.lang ?: subtitle.title ?: stringResource(Res.string.player_track_subtitle)
    }
}

@Composable
internal fun subtitleTrackLabel(subtitle: Subtitle): String = subtitleSubtitleTitle(subtitle)

@Composable
internal fun subtitleDetail(subtitle: Subtitle): String? {
    val parts =
        buildList {
            subtitle.title?.takeIf { it.isNotEmpty() }?.let { add(it) }
            subtitle.addonName?.takeIf { it.isNotEmpty() }?.let { add(it) }
            if (subtitle.aiTranslated) add(stringResource(Res.string.player_track_ai))
            if (subtitle.fromTrusted) add(stringResource(Res.string.player_track_trusted))
        }
    return parts.takeIf { it.isNotEmpty() }?.joinToString(" · ")
}
