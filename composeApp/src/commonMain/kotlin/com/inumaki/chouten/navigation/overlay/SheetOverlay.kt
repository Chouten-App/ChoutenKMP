package com.inumaki.chouten.navigation.overlay

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.inumaki.chouten.dev.DevClientManager
import com.inumaki.core.ui.components.SharedElement
import com.inumaki.core.ui.model.AppConfig
import com.inumaki.core.ui.model.AppRoute
import com.inumaki.core.ui.model.SettingsRoute
import dev.chouten.features.settings.SettingsView
import dev.chouten.features.settings.SettingsViewModel

/**
 * Renders sheet overlays for specific routes.
 *
 * Sheet routes are displayed as modal sheets on top of the main content
 * (e.g., Settings). They typically use shared element transitions for smooth animations.
 *
 * @param route The route to render
 * @param appConfig App configuration for navigation
 * @param dataStore DataStore for persisting settings
 * @param devClientManager Manager for dev client connections
 * @param maxHeight Maximum height of the container for positioning
 */
@Composable
fun SheetOverlay(
    route: AppRoute,
    appConfig: AppConfig,
    dataStore: DataStore<Preferences>,
    devClientManager: DevClientManager,
    maxHeight: Dp
) {
    when (route) {
        is SettingsRoute -> {
            val viewModel = appConfig.navScope.viewModelStore.get("settings") {
                SettingsViewModel(
                    dataStore = dataStore,
                    onCliChange = { cliIP ->
                        devClientManager.initialize(cliIP)
                    }
                )
            }

            SharedElement(
                key = "settings_morph",
                offset = Offset(x = 0f, y = maxHeight.value * 0.3f)
            ) {
                SettingsView(
                    viewModel = viewModel,
                    appConfig = appConfig,
                    modifier = Modifier
                        .offset(y = maxHeight * 0.1f)
                        .fillMaxWidth()
                        .fillMaxHeight(0.9f)
                )
            }
        }

        else -> {
            // No sheet overlay for this route
        }
    }
}