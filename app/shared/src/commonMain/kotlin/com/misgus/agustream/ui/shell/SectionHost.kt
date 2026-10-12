package com.misgus.agustream.ui.shell

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.misgus.agustream.ui.home.HomeScreen
import com.misgus.agustream.ui.home.HomeViewModel
import com.misgus.agustream.ui.library.LibraryScreen
import com.misgus.agustream.ui.library.LibraryViewModel
import com.misgus.agustream.ui.screens.PlaceholderScreen
import com.misgus.agustream.ui.screens.SettingsScreen
import com.misgus.agustream.ui.services.LocalAppServices
import com.misgus.agustream.ui.streams.StreamsRequest
import kotlinx.coroutines.launch

/**
 * Content of the shell: shows [selected].
 *
 * Section ViewModels live in the root store, so their loaded data survives
 * switching sections; a section not yet visited never fires its own requests
 * because the load runs from the screen's `LaunchedEffect`.
 */
@Composable
fun SectionHost(
    selected: AppSection,
    navigator: ContentNavigator,
    onSelectSection: (AppSection) -> Unit,
    onOpenStreams: (StreamsRequest) -> Unit,
) {
    val services = LocalAppServices.current
    when (selected) {
        AppSection.HOME -> {
            val viewModel: HomeViewModel =
                viewModel {
                    HomeViewModel(
                        account = services.account,
                        catalogs = services.catalogs,
                        library = services.library,
                        progress = services.progress,
                        badges = services.watchedBadges,
                    )
                }
            HomeScreen(viewModel, navigator, onOpenStreams, onOpenSettings = { onSelectSection(AppSection.SETTINGS) })
        }
        AppSection.LIBRARY -> {
            val viewModel: LibraryViewModel =
                viewModel {
                    LibraryViewModel(
                        account = services.account,
                        library = services.library,
                        progress = services.progress,
                        badges = services.watchedBadges,
                    )
                }
            LibraryScreen(viewModel, navigator, onOpenSettings = { onSelectSection(AppSection.SETTINGS) })
        }
        AppSection.SETTINGS -> {
            val snackbar = LocalSnackbarHost.current
            val scope = rememberCoroutineScope()
            SettingsScreen(
                onOpenPlayer = { target ->
                    if (services.playbackSupported) {
                        navigator.push(ContentRoute.Player(target))
                    } else {
                        scope.launch {
                            snackbar?.showSnackbar("Playback is not available on this target yet.")
                        }
                    }
                },
            )
        }
        else -> PlaceholderScreen(selected)
    }
}
