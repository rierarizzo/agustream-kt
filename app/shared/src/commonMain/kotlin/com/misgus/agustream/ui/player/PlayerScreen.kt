package com.misgus.agustream.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.misgus.agustream.core.model.MetaVideo
import com.misgus.agustream.core.model.Subtitle
import com.misgus.agustream.core.player.MediaTrack
import com.misgus.agustream.core.player.PlaybackTarget
import com.misgus.agustream.core.util.runCatchingCancellable
import com.misgus.agustream.ui.services.LocalAppServices
import com.misgus.agustream.ui.shell.LocalSnackbarHost
import com.misgus.agustream.ui.theme.AppColors
import com.misgus.agustream.ui.theme.AppSpacing
import com.misgus.agustream.ui.video.VideoSurface
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Integrated player: custom controls, track selection, fullscreen, autoplay of
 * the next episode and watch-progress reporting.
 */
@Composable
fun PlayerScreen(
    target: PlaybackTarget,
    onBack: () -> Unit,
    onPlayNext: (PlaybackTarget) -> Unit = {},
) {
    val services = LocalAppServices.current
    val player = services.player
    val chrome = services.chrome
    val window = services.window
    val snackbar = LocalSnackbarHost.current
    val scope = rememberCoroutineScope()
    val state by player.state.collectAsStateWithLifecycle()
    val reporter = remember(target) { PlaybackProgressReporter(services.progress, target) }
    val focusRequester = remember { FocusRequester() }

    var controlsVisible by remember { mutableStateOf(true) }
    var lastInteraction by remember { mutableLongStateOf(0L) }
    var addonSubtitles by remember { mutableStateOf<List<Subtitle>>(emptyList()) }
    var showSubtitleMenu by remember { mutableStateOf(false) }
    var showAudioMenu by remember { mutableStateOf(false) }
    // Volume to restore when mute is toggled back on.
    var volumeBeforeMute by remember { mutableStateOf(100.0) }

    DisposableEffect(target) {
        chrome.enterImmersive()
        player.open(target)
        onDispose {
            chrome.exitImmersive()
            scope.launch { reporter.flush(force = true) }
            player.stop()
        }
    }

    LaunchedEffect(target) {
        reporter.resumePosition()?.let { position ->
            // Wait until the media is parsed, then jump to the saved position.
            // A timeout keeps a stream that never reports a duration from
            // leaving the resume pending forever.
            withTimeoutOrNull(RESUME_TIMEOUT) {
                player.state.first { it.duration > Duration.ZERO }
            }
            player.seek(position)
        }
        val id = target.videoId ?: target.contentId
        val type = target.contentType
        if (id != null && type != null) {
            addonSubtitles =
                runCatchingCancellable {
                    services.subtitles.all(type, id)
                }.getOrDefault(emptyList())
        }
    }

    LaunchedEffect(player) {
        player.state.collect { snapshot ->
            reporter.update(snapshot.position, snapshot.duration)
            if (!snapshot.playing || snapshot.completed) reporter.flush(force = true) else reporter.flush()
            if (snapshot.completed && target.videoId != null) {
                playNextIfPossible(target, services)?.let(onPlayNext)
            }
        }
    }

    // Auto-hide the controls after 3 seconds of playback.
    LaunchedEffect(state.playing, lastInteraction) {
        if (!state.playing) {
            controlsVisible = true
            return@LaunchedEffect
        }
        controlsVisible = true
        delay(3000)
        controlsVisible = false
    }

    val revealControls = {
        lastInteraction = lastInteraction + 1
        controlsVisible = true
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.Black)
                .focusRequester(focusRequester)
                .focusable()
                .pointerInput(Unit) {
                    // Any pointer activity reveals the controls, which then
                    // auto-hide again after the inactivity delay.
                    awaitPointerEventScope {
                        while (true) {
                            awaitPointerEvent(PointerEventPass.Initial)
                            lastInteraction = lastInteraction + 1
                            controlsVisible = true
                        }
                    }
                }.onPreviewKeyEvent { event ->
                    if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                    SEEK_PERCENT_KEYS[event.key]?.let { fraction ->
                        if (state.duration > Duration.ZERO) player.seek(state.duration * fraction)
                        revealControls()
                        return@onPreviewKeyEvent true
                    }
                    when (event.key) {
                        Key.Spacebar, Key.K -> {
                            player.playOrPause()
                            true
                        }
                        Key.DirectionLeft, Key.J -> {
                            player.seekBy(Duration.parse("-10s"))
                            revealControls()
                            true
                        }
                        Key.DirectionRight, Key.L -> {
                            player.seekBy(Duration.parse("10s"))
                            revealControls()
                            true
                        }
                        Key.DirectionUp -> {
                            player.setVolume((state.volume + VOLUME_STEP).coerceAtMost(100.0))
                            revealControls()
                            true
                        }
                        Key.DirectionDown -> {
                            player.setVolume((state.volume - VOLUME_STEP).coerceAtLeast(0.0))
                            revealControls()
                            true
                        }
                        Key.M -> {
                            if (state.volume > 0.0) {
                                volumeBeforeMute = state.volume
                                player.setVolume(0.0)
                            } else {
                                player.setVolume(volumeBeforeMute.coerceAtLeast(1.0))
                            }
                            revealControls()
                            true
                        }
                        Key.F -> {
                            window.setFullScreen(!window.isFullScreen.value)
                            true
                        }
                        Key.Escape -> {
                            onBack()
                            true
                        }
                        else -> false
                    }
                },
    ) {
        LaunchedEffect(Unit) { focusRequester.requestFocus() }

        VideoSurface(player, Modifier.fillMaxSize())
        if (state.buffering) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        }
        PlayerControls(
            title = target.title,
            playing = state.playing,
            position = state.position,
            duration = state.duration,
            volume = state.volume,
            visible = controlsVisible,
            onPlayPause = {
                player.playOrPause()
                revealControls()
            },
            onSeek = {
                player.seek(it)
                revealControls()
            },
            onSeekBy = {
                player.seekBy(it)
                revealControls()
            },
            onVolumeChanged = {
                player.setVolume(it)
                revealControls()
            },
            onToggleFullscreen = { window.setFullScreen(!window.isFullScreen.value) },
            onSubtitles = { showSubtitleMenu = true },
            onAudio = { showAudioMenu = true },
            onBack = onBack,
        )

        state.error?.let { error ->
            LaunchedEffect(error) {
                snackbar?.showSnackbar("Playback error: $error")
            }
        }
    }

    if (showSubtitleMenu) {
        TrackMenu(
            title = "Subtitles",
            rows =
                buildList {
                    add(
                        TrackRow(label = "Off", selected = state.selectedSubtitleId == null) {
                            player.selectSubtitleTrack(null)
                        },
                    )
                    state.subtitleTracks.forEach { track ->
                        add(
                            TrackRow(
                                label = trackLabel(track),
                                selected = state.selectedSubtitleId == track.id,
                            ) { player.selectSubtitleTrack(track) },
                        )
                    }
                    addonSubtitles.forEach { subtitle ->
                        val track =
                            MediaTrack(
                                id = subtitle.url,
                                title = subtitleSubtitleTitle(subtitle),
                                language = subtitle.langCode ?: subtitle.lang,
                                uri = subtitle.url,
                                isExternal = true,
                            )
                        add(
                            TrackRow(
                                label = subtitleTrackLabel(subtitle),
                                detail = subtitleDetail(subtitle),
                                selected = state.selectedSubtitleId == subtitle.url,
                            ) { player.selectSubtitleTrack(track) },
                        )
                    }
                },
            onDismiss = { showSubtitleMenu = false },
        )
    }

    if (showAudioMenu) {
        TrackMenu(
            title = "Audio",
            rows =
                state.audioTracks.map { track ->
                    TrackRow(
                        label = trackLabel(track),
                        selected = state.selectedAudioId == track.id,
                    ) { player.selectAudioTrack(track) }
                },
            onDismiss = { showAudioMenu = false },
        )
    }
}

private data class TrackRow(
    val label: String,
    val detail: String? = null,
    val selected: Boolean,
    val onSelect: () -> Unit,
)

@Composable
private fun TrackMenu(
    title: String,
    rows: List<TrackRow>,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
        title = { Text(title) },
        text = {
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                if (rows.isEmpty()) {
                    Text("No tracks available.", style = MaterialTheme.typography.bodyMedium)
                }
                rows.forEach { row ->
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    row.onSelect()
                                    onDismiss()
                                }.padding(vertical = AppSpacing.sm),
                    ) {
                        Text(
                            text = if (row.selected) "● ${row.label}" else "○ ${row.label}",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        row.detail?.let {
                            Text(it, style = MaterialTheme.typography.bodySmall, color = AppColors.textSecondary)
                        }
                    }
                }
            }
        },
    )
}

private fun trackLabel(track: MediaTrack): String =
    track.title?.takeIf { it.isNotEmpty() } ?: track.language?.takeIf { it.isNotEmpty() } ?: track.id

private fun subtitleSubtitleTitle(subtitle: Subtitle): String {
    val name = subtitle.languageName
    val code = subtitle.langCode ?: subtitle.lang
    return if (name != null) {
        if (code.isNullOrEmpty()) name else "$name ($code)"
    } else {
        subtitle.lang ?: subtitle.title ?: "Subtitle"
    }
}

private fun subtitleTrackLabel(subtitle: Subtitle): String = subtitleSubtitleTitle(subtitle)

private fun subtitleDetail(subtitle: Subtitle): String? {
    val parts =
        buildList {
            subtitle.title?.takeIf { it.isNotEmpty() }?.let { add(it) }
            subtitle.addonName?.takeIf { it.isNotEmpty() }?.let { add(it) }
            if (subtitle.aiTranslated) add("AI")
            if (subtitle.fromTrusted) add("Trusted")
        }
    return parts.takeIf { it.isNotEmpty() }?.joinToString(" · ")
}

/** Best-effort autoplay: finds the next episode and its first direct stream. */
private suspend fun playNextIfPossible(
    target: PlaybackTarget,
    services: com.misgus.agustream.ui.services.AppServices,
): PlaybackTarget? {
    val seriesId = target.contentId ?: return null
    return runCatchingCancellable {
        val meta = services.metadata.detail("series", seriesId) ?: return@runCatchingCancellable null
        val episodes =
            meta.videos
                .filter { (it.season ?: 0) > 0 && (it.episode ?: 0) > 0 }
                .sortedBy { episodeOrder(it) }
        val index = episodes.indexOfFirst { it.season == target.season && it.episode == target.episode }
        if (index < 0 || index + 1 >= episodes.size) return@runCatchingCancellable null
        val next = episodes[index + 1]
        val nextId = "$seriesId:${next.season}:${next.episode}"
        val groups = services.streams.all("series", nextId)
        val stream =
            groups.firstNotNullOfOrNull { group ->
                group.streams.firstOrNull { it.isDirectPlayable }
            } ?: return@runCatchingCancellable null
        val url = stream.url ?: return@runCatchingCancellable null
        PlaybackTarget.fromContent(
            type = "series",
            id = nextId,
            title = "${meta.name} · S${next.season}E${next.episode}",
            source = url,
            httpHeaders = stream.httpHeaders,
        )
    }.getOrNull()
}

private fun episodeOrder(video: MetaVideo): Int = (video.season ?: 0) * 1000 + (video.episode ?: 0)

/** How long to wait for a parsed duration before resuming anyway. */
private val RESUME_TIMEOUT = 5.seconds

/** Volume change applied by the up/down arrow shortcuts, in percent. */
private const val VOLUME_STEP = 5.0

/**
 * Number-key seek targets: `0`…`9` jump to 0 %…90 % of the media, the
 * convention used by the major streaming players.
 */
private val SEEK_PERCENT_KEYS =
    mapOf(
        Key.Zero to 0.0,
        Key.One to 0.1,
        Key.Two to 0.2,
        Key.Three to 0.3,
        Key.Four to 0.4,
        Key.Five to 0.5,
        Key.Six to 0.6,
        Key.Seven to 0.7,
        Key.Eight to 0.8,
        Key.Nine to 0.9,
    )
