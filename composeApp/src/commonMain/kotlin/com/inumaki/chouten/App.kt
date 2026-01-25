package com.inumaki.chouten

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.*
import androidx.navigation.compose.rememberNavController
import com.inumaki.chouten.common.getFeatures
import com.inumaki.core.ui.AppScaffold
import com.inumaki.core.ui.model.AppConfig
import com.inumaki.core.ui.model.DiscoverRoute
import com.inumaki.core.ui.model.GlobalState
import com.inumaki.core.ui.model.HomeRoute
import com.inumaki.core.ui.model.NavigationScope
import com.inumaki.core.ui.model.RepoRoute
import com.inumaki.core.ui.model.SettingsRoute
import com.inumaki.features.discover.DiscoverView
import com.inumaki.features.discover.DiscoverViewModel
import com.inumaki.features.home.HomeView
import com.inumaki.features.repo.RepoView
import dev.chouten.features.settings.SettingsView


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(provider: HeadingSource) {
    val navController = rememberNavController()
    val (featureEntries, uiConfigProviders) = getFeatures()

    val navScope = remember { NavigationScope() }
    val discoverVm = navScope.viewModelStore.get("discover") { DiscoverViewModel() }

    LaunchedEffect(provider.heading) {
        provider.heading.collect { newValue ->
            GlobalState.setAngle(newValue)
        }
    }

    AppScaffold(
        provider.heading,
        AppConfig(
            navController,
            navScope,
            DiscoverRoute,
            featureEntries,
            uiConfigProviders
        ),
        renderFullscreen = { route ->
            when (route) {
                is HomeRoute -> HomeView()
                is DiscoverRoute -> DiscoverView(discoverVm)
                is RepoRoute -> RepoView()
                else -> {}
            }
        },
        renderSheet = { route, onDismiss ->
            when (route) {
                is SettingsRoute -> ModalBottomSheet(onDismissRequest = onDismiss) {
                    SettingsView()
                }
                else -> {}
            }
        }
    )
}