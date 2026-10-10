package com.misgus.agustream.ui.shell

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.misgus.agustream.ui.home.HomeScreen
import com.misgus.agustream.ui.home.HomeViewModel
import com.misgus.agustream.ui.library.LibraryScreen
import com.misgus.agustream.ui.library.LibraryViewModel
import com.misgus.agustream.ui.screens.PlaceholderScreen
import com.misgus.agustream.ui.screens.SettingsScreen
import com.misgus.agustream.ui.services.LocalAppServices
import com.misgus.agustream.ui.streams.StreamsRequest

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
    onOpenStreams: (StreamsRequest) -> Unit,
) {
    val services = LocalAppServices.current
    when (selected) {
        AppSection.HOME -> {
            val viewModel: HomeViewModel = viewModel {
                HomeViewModel(
                    account = services.account,
                    catalogs = services.catalogs,
                    library = services.library,
                    progress = services.progress,
                    badges = services.watchedBadges,
                )
            }
            HomeScreen(viewModel, navigator, onOpenStreams)
        }
        AppSection.LIBRARY -> {
            val viewModel: LibraryViewModel = viewModel {
                LibraryViewModel(
                    account = services.account,
                    library = services.library,
                    progress = services.progress,
                    badges = services.watchedBadges,
                )
            }
            LibraryScreen(viewModel, navigator)
        }
        AppSection.SETTINGS -> SettingsScreen(
            onOpenPlayer = { target -> navigator.push(ContentRoute.Player(target)) },
        )
        else -> PlaceholderScreen(selected)
    }
}
