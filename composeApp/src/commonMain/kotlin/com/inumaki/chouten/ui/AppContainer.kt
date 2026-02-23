package com.inumaki.chouten.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.inumaki.chouten.HeadingSource
import com.inumaki.chouten.dev.DevClientManager
import com.inumaki.chouten.navigation.AppNavHost
import com.inumaki.chouten.navigation.NavigationState
import com.inumaki.chouten.navigation.overlay.FullscreenOverlay
import com.inumaki.chouten.navigation.overlay.SheetOverlay
import com.inumaki.chouten.navigation.overlay.SheetScrim
import com.inumaki.core.ui.AppScaffold
import com.inumaki.core.ui.components.SharedElementOverlay
import com.inumaki.core.ui.model.AppConfig
import com.inumaki.core.ui.model.PresentationStyle
import com.inumaki.core.ui.model.presentationStyle
import com.inumaki.core.ui.theme.AppTheme

/**
 * Main app container that manages the navigation hierarchy and overlays.
 *
 * Structure:
 * - AppScaffold (bottom nav, etc.)
 *   - NavHost (main navigation)
 *   - Fullscreen overlays (discover, home, repo)
 * - Sheet scrim (dimmed background)
 * - Sheet overlays (settings, etc.)
 * - Shared element overlay
 */
@Composable
fun AppContainer(
    headingSource: HeadingSource,
    appConfig: AppConfig,
    dataStore: DataStore<Preferences>,
    devClientManager: DevClientManager,
    modifier: Modifier = Modifier
) {
    // Observe navigation state
    val backStackEntries by appConfig.navController.currentBackStack.collectAsState()

    val navigationState = remember(backStackEntries, appConfig.featureEntries) {
        NavigationState.from(backStackEntries, appConfig.featureEntries)
    }

    val showSheetOverlay = navigationState.topRoute?.presentationStyle() == PresentationStyle.Sheet

    Box(modifier = modifier.fillMaxSize()) {
        // Main scaffold with navigation
        AppScaffold(
            headingSource.heading,
            appConfig
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Primary navigation host
                AppNavHost(appConfig = appConfig)

                // Fullscreen overlays (e.g., Discover, Home, Repo)
                navigationState.fullscreenRoute?.let { route ->
                    FullscreenOverlay(
                        route = route,
                        navScope = appConfig.navScope
                    )
                }
            }
        }

        // Sheet background scrim
        if (showSheetOverlay) {
            SheetScrim(
                onDismiss = { appConfig.navController.popBackStack() }
            )
        }

        // Sheet overlays (e.g., Settings)
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            CompositionLocalProvider(
                LocalContentColor provides AppTheme.colors.fg
            ) {
                navigationState.topRoute?.let { route ->
                    SheetOverlay(
                        route = route,
                        appConfig = appConfig,
                        dataStore = dataStore,
                        devClientManager = devClientManager,
                        maxHeight = maxHeight
                    )
                }

                // Shared element transitions
                SharedElementOverlay()
            }
        }
    }
}