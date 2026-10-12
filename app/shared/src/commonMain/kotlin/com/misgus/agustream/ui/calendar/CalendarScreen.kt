package com.misgus.agustream.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.misgus.agustream.core.model.LibraryItem
import com.misgus.agustream.ui.components.Message
import com.misgus.agustream.ui.components.PosterImage
import com.misgus.agustream.ui.components.toUserMessage
import com.misgus.agustream.ui.shell.ContentNavigator
import com.misgus.agustream.ui.shell.ContentRoute
import com.misgus.agustream.ui.theme.AppColors
import com.misgus.agustream.ui.theme.AppRadii
import com.misgus.agustream.ui.theme.AppSpacing
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlin.time.Clock

/**
 * Calendar section: a full-screen month grid where each day shows the episodes
 * of the Library's series that air on it.
 */
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel,
    navigator: ContentNavigator,
    onOpenSettings: () -> Unit,
) {
    LaunchedEffect(Unit) { viewModel.load() }
    val state by viewModel.state.collectAsStateWithLifecycle()

    CalendarContent(
        state = state,
        onOpenSettings = onOpenSettings,
        onOpenDetail = { navigator.push(ContentRoute.Detail(it, knownSaved = true)) },
    )
}

@Composable
private fun CalendarContent(
    state: CalendarUiState,
    onOpenSettings: () -> Unit,
    onOpenDetail: (LibraryItem) -> Unit,
) {
    when {
        !state.isSignedIn ->
            Message(
                icon = Icons.Outlined.Lock,
                title = "Not signed in",
                message = "Sign in to see your upcoming episodes.",
                actionLabel = "Sign in",
                onAction = onOpenSettings,
            )
        state.error != null && state.entries.isEmpty() ->
            Message(
                icon = Icons.Outlined.ErrorOutline,
                title = "Could not load the calendar",
                message = state.error.toUserMessage(),
            )
        state.hasLoaded && state.entries.isEmpty() ->
            Message(
                icon = Icons.Outlined.CalendarMonth,
                title = "No upcoming episodes",
                message = "The series in your library have no announced episodes in the coming months.",
            )
        // The grid paints immediately and fills in as series resolve.
        else ->
            MonthCalendar(
                entries = state.entries,
                isLoading = state.isLoading,
                onOpenDetail = onOpenDetail,
            )
    }
}

@Composable
private fun MonthCalendar(
    entries: List<CalendarEntry>,
    isLoading: Boolean,
    onOpenDetail: (LibraryItem) -> Unit,
) {
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
    val byDate = remember(entries) { entries.groupBy { it.date } }
    var month by remember { mutableStateOf(LocalDate(today.year, today.month, 1)) }

    Column(Modifier.fillMaxSize()) {
        MonthHeader(
            month = month,
            isLoading = isLoading,
            onPrevious = { month = month.plus(-1, DateTimeUnit.MONTH) },
            onNext = { month = month.plus(1, DateTimeUnit.MONTH) },
        )
        WeekdayHeader()
        MonthGrid(
            month = month,
            today = today,
            byDate = byDate,
            onOpenDetail = onOpenDetail,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun MonthHeader(
    month: LocalDate,
    isLoading: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.xl, vertical = AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous month")
        }
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = formatMonth(month),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
            )
            if (isLoading) {
                Spacer(Modifier.width(AppSpacing.sm))
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
            }
        }
        IconButton(onClick = onNext) {
            Icon(Icons.Default.ChevronRight, contentDescription = "Next month")
        }
    }
}

@Composable
private fun WeekdayHeader() {
    Row(Modifier.fillMaxWidth().padding(horizontal = AppSpacing.xl)) {
        for (day in WEEKDAY_LABELS) {
            Text(
                text = day,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall,
                color = AppColors.textSecondary,
                modifier = Modifier.weight(1f).padding(vertical = AppSpacing.xs),
            )
        }
    }
}

@Composable
private fun MonthGrid(
    month: LocalDate,
    today: LocalDate,
    byDate: Map<LocalDate, List<CalendarEntry>>,
    onOpenDetail: (LibraryItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val daysInMonth = month.daysUntil(month.plus(1, DateTimeUnit.MONTH))
    val leading = month.dayOfWeek.ordinal
    val cells: List<LocalDate?> =
        buildList {
            repeat(leading) { add(null) }
            for (day in 1..daysInMonth) add(LocalDate(month.year, month.month, day))
            while (size % DAYS_PER_WEEK != 0) add(null)
        }

    // Each week takes an equal share of the available height, so the grid fills
    // the window instead of being a fixed-height block that leaves a gap.
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(start = AppSpacing.xl, end = AppSpacing.xl, bottom = AppSpacing.lg),
    ) {
        cells.chunked(DAYS_PER_WEEK).forEach { week ->
            Row(Modifier.fillMaxWidth().weight(1f)) {
                week.forEach { date ->
                    DayCell(
                        date = date,
                        isToday = date == today,
                        entries = date?.let { byDate[it].orEmpty() }.orEmpty(),
                        onOpenDetail = onOpenDetail,
                        modifier = Modifier.weight(1f).fillMaxSize(),
                    )
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate?,
    isToday: Boolean,
    entries: List<CalendarEntry>,
    onOpenDetail: (LibraryItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (isToday) AppColors.accent else AppColors.divider
    Column(
        modifier =
            modifier
                .clipToBounds()
                .border(1.dp, borderColor)
                .background(if (entries.isEmpty()) AppColors.background else AppColors.surface)
                .padding(AppSpacing.xs),
    ) {
        date?.let {
            Text(
                text = it.day.toString(),
                style = MaterialTheme.typography.labelMedium,
                color = if (isToday) AppColors.accent else AppColors.textSecondary,
            )
        }
        Box(Modifier.weight(1f)) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                entries.take(MAX_ENTRIES_PER_DAY).forEach { entry ->
                    EpisodeChip(entry = entry, onTap = { onOpenDetail(entry.item) })
                }
                if (entries.size > MAX_ENTRIES_PER_DAY) {
                    Text(
                        text = "+${entries.size - MAX_ENTRIES_PER_DAY} more",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppColors.textSecondary,
                    )
                }
            }
        }
    }
}

@Composable
private fun EpisodeChip(
    entry: CalendarEntry,
    onTap: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(AppRadii.sm))
                .clickable(onClick = onTap)
                .padding(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .size(width = 30.dp, height = 42.dp)
                    .clip(RoundedCornerShape(AppRadii.sm)),
        ) {
            PosterImage(entry.item.poster, Modifier.fillMaxSize())
        }
        Spacer(Modifier.width(AppSpacing.sm))
        Column(Modifier.weight(1f)) {
            Text(
                text = entry.item.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelMedium,
                color = AppColors.textPrimary,
            )
            Text(
                text = episodeLabel(entry),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelSmall,
                color = AppColors.accent,
            )
            entry.video.title?.takeIf { it.isNotEmpty() }?.let {
                Text(
                    text = it,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelSmall,
                    color = AppColors.textSecondary,
                )
            }
        }
    }
}

private fun episodeLabel(entry: CalendarEntry): String {
    val video = entry.video
    return buildString {
        video.season?.let { append("S$it") }
        video.episode?.let { append("E$it") }
        if (isEmpty()) append(video.title.orEmpty())
    }
}

/** Formats a month as `May 2025`. */
private fun formatMonth(month: LocalDate): String {
    val name =
        month.month.name
            .lowercase()
            .replaceFirstChar { it.uppercase() }
    return "$name ${month.year}"
}

private const val DAYS_PER_WEEK = 7

/** One entry per day: the richer chip (poster, series, episode) needs the room. */
private const val MAX_ENTRIES_PER_DAY = 1
private val WEEKDAY_LABELS = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
