package com.inumaki.core.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import com.inumaki.core.ui.components.AppBottomBar
import com.inumaki.core.ui.components.AppTopBar
import com.inumaki.core.ui.model.AppConfig
import com.inumaki.core.ui.model.AppRoute
import com.inumaki.core.ui.model.FeatureEntry
import com.inumaki.core.ui.model.NavigationScope
import com.inumaki.core.ui.model.PresentationStyle
import com.inumaki.core.ui.model.SettingsRoute
import com.inumaki.core.ui.model.presentationStyle
import com.inumaki.core.ui.theme.AppTheme
import kotlinx.coroutines.flow.StateFlow

@Composable
fun rememberLastFullscreenRoute(
    navController: NavHostController,
    featureEntries: List<FeatureEntry>
): MutableState<AppRoute?> {
    val lastFullscreen = remember { mutableStateOf<AppRoute?>(null) }

    val backStackEntry by navController.currentBackStackEntryAsState()
    LaunchedEffect(backStackEntry) {
        backStackEntry?.let { entry ->
            // Try to get current route
            val current = featureEntries.mapNotNull { it.tryCreateRoute(entry) }
                .firstOrNull { it.presentationStyle() == PresentationStyle.Fullscreen }

            if (current != null) {
                lastFullscreen.value = current
            }
            // If top is a sheet, lastFullscreen keeps the previous fullscreen
        }
    }

    return lastFullscreen
}

@Composable
fun AppScaffold(
    heading: StateFlow<Float>,
    appConfig: AppConfig,
    renderFullscreen: @Composable (AppRoute) -> Unit,
    renderSheet: @Composable (AppRoute, onDismiss: () -> Unit) -> Unit
) {
    val angle = heading.collectAsState()
    val lastFullscreen = rememberLastFullscreenRoute(appConfig.navController, appConfig.featureEntries)

    val backStackEntry by appConfig.navController.currentBackStackEntryAsState()

    val currentRoute: AppRoute? = backStackEntry?.let { entry ->
        appConfig.featureEntries
            .asSequence()
            .mapNotNull { feature -> feature.tryCreateRoute(entry) }
            .firstOrNull()
    }

    val topConfig = currentRoute?.let { route ->
        appConfig.uiConfigProvider.asSequence().mapNotNull { it.topBarConfig(route = route, navController = appConfig.navController) }
            .firstOrNull()
    }

    AppTheme() {
        Scaffold(
            topBar = { AppTopBar(topConfig, angle.value) },
            bottomBar = { AppBottomBar(angle.value, appConfig.navController) },
            containerColor = AppTheme.colors.background,
            contentColor = AppTheme.colors.fg,
            modifier = Modifier.fillMaxSize()
        ) { padding ->
            Box(Modifier.fillMaxSize()) {
                // Register all routes
                NavHost(
                    navController = appConfig.navController,
                    startDestination = "Discover",
                    modifier = Modifier.fillMaxSize()
                ) {
                    appConfig.featureEntries.forEach { it.register(this, appConfig.navController, appConfig.navScope) }
                }
                // Render last fullscreen
                lastFullscreen.value?.let { renderFullscreen(it) }

                // Render top sheet if any
                val backStackEntry by appConfig.navController.currentBackStackEntryAsState()
                val sheetRoute = backStackEntry?.let { entry ->
                    appConfig.featureEntries.mapNotNull { it.tryCreateRoute(entry) }
                        .firstOrNull { it.presentationStyle() == PresentationStyle.Sheet }
                }

                sheetRoute?.let { renderSheet(it) { appConfig.navController.popBackStack() } }
            }
        }
    }
}

