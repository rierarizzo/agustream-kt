package com.misgus.pengu.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.misgus.pengu.app.shared.generated.resources.Res
import com.misgus.pengu.app.shared.generated.resources.detail_cast
import com.misgus.pengu.app.shared.generated.resources.detail_country
import com.misgus.pengu.app.shared.generated.resources.detail_crew
import com.misgus.pengu.app.shared.generated.resources.detail_director
import com.misgus.pengu.app.shared.generated.resources.detail_episode_generic
import com.misgus.pengu.app.shared.generated.resources.detail_released
import com.misgus.pengu.app.shared.generated.resources.detail_writer
import com.misgus.pengu.core.model.ContentTypes
import com.misgus.pengu.core.model.LibraryItem
import com.misgus.pengu.core.model.MetaPerson
import com.misgus.pengu.ui.detail.CrewMember
import com.misgus.pengu.ui.detail.CrewRole
import com.misgus.pengu.ui.detail.DetailUiState
import com.misgus.pengu.ui.theme.AppSpacing
import com.misgus.pengu.ui.theme.safeBottomInset
import org.jetbrains.compose.resources.stringResource

/**
 * Scrollable body of the detail screen: episodes, cast/crew, release details and
 * the "more like this" row.
 *
 * Kept apart from the billboard so each file stays focused on one part of the
 * screen.
 */
@Composable
internal fun DetailScrollContent(
    state: DetailUiState,
    item: LibraryItem,
    openStreams: (String, String, String) -> Unit,
    onOpenDetail: (LibraryItem) -> Unit,
) {
    if (state.isSeries && state.episodes.isNotEmpty()) {
        Spacer(Modifier.height(AppSpacing.lg))
        val episodeFallback = stringResource(Res.string.detail_episode_generic)
        EpisodesSection(state.episodes) { video ->
            openStreams(ContentTypes.SERIES, video.id, "${state.name} · ${video.title ?: episodeFallback}")
        }
    }

    val detailEntries =
        buildList {
            state.released?.takeIf { it.isNotEmpty() }?.let { add(stringResource(Res.string.detail_released) to it) }
            state.country?.takeIf { it.isNotEmpty() }?.let { add(stringResource(Res.string.detail_country) to it) }
        }
    if (state.cast.isNotEmpty()) {
        Spacer(Modifier.height(AppSpacing.lg))
        PeopleRow(stringResource(Res.string.detail_cast), state.cast)
    }
    if (state.crew.isNotEmpty()) {
        val directorLabel = stringResource(Res.string.detail_director)
        val writerLabel = stringResource(Res.string.detail_writer)
        val crew = state.crew.map { member -> member.toMetaPerson(directorLabel, writerLabel) }
        Spacer(Modifier.height(AppSpacing.lg))
        PeopleRow(stringResource(Res.string.detail_crew), crew)
    }
    if (detailEntries.isNotEmpty()) {
        Spacer(Modifier.height(AppSpacing.lg))
        DetailsSection(detailEntries)
    }
    if (state.similar.isNotEmpty()) {
        Spacer(Modifier.height(AppSpacing.lg))
        SimilarRow(state.similar) { preview ->
            onOpenDetail(LibraryItem.fromPreview(preview, addonBaseUrl = item.addonBaseUrl))
        }
    }
    Spacer(Modifier.height(AppSpacing.xl + safeBottomInset))
}

/** Maps a crew member to the shared [MetaPerson] shape, with localized role labels. */
private fun CrewMember.toMetaPerson(
    directorLabel: String,
    writerLabel: String,
): MetaPerson =
    MetaPerson(
        name = name,
        character =
            roles.joinToString(", ") { role ->
                if (role == CrewRole.DIRECTOR) directorLabel else writerLabel
            },
    )
