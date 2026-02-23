package com.inumaki.chouten.navigation.overlay

import androidx.compose.runtime.Composable
import com.inumaki.core.ui.model.AppRoute
import com.inumaki.core.ui.model.DiscoverRoute
import com.inumaki.core.ui.model.HomeRoute
import com.inumaki.core.ui.model.NavigationScope
import com.inumaki.core.ui.model.RepoRoute
import com.inumaki.features.discover.DiscoverView
import com.inumaki.features.discover.DiscoverViewModel
import com.inumaki.features.home.HomeView
import com.inumaki.features.repo.RepoView

/**
 * Renders fullscreen overlays for specific routes.
 *
 * Fullscreen routes are displayed on top of the main navigation host
 * and typically represent the main screens of the app (Discover, Home, Repo).
 *
 * ViewModels are scoped to the navigation scope to survive configuration changes.
 */
@Composable
fun FullscreenOverlay(
    route: AppRoute,
    navScope: NavigationScope
) {
    when (route) {
        is DiscoverRoute -> {
            val viewModel = navScope.viewModelStore.get("discover") {
                DiscoverViewModel()
            }
            DiscoverView(viewModel)
        }

        is HomeRoute -> {
            HomeView()
        }

        is RepoRoute -> {
            RepoView()
        }

        else -> {
            // No fullscreen overlay for this route
        }
    }
}