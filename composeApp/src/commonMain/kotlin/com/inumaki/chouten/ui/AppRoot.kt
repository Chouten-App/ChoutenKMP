package com.inumaki.chouten.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.navigation.compose.rememberNavController
import com.inumaki.chouten.HeadingSource
import com.inumaki.chouten.common.getFeatures
import com.inumaki.chouten.dev.DevClientManager
import com.inumaki.core.ui.model.AppConfig
import com.inumaki.core.ui.model.DiscoverRoute
import com.inumaki.core.ui.model.GlobalState
import com.inumaki.core.ui.model.NavigationScope
import com.inumaki.core.ui.theme.AppTheme
import dev.chouten.runners.relay.NativeBridge
import dev.chouten.runners.relay.RelayLogger

/**
 * Root composable that sets up the app configuration and theme.
 * Handles initialization of core systems like navigation, DI, and dev tools.
 */
@Composable
fun AppRoot(
    headingSource: HeadingSource,
    dataStore: DataStore<Preferences>
) {
    // Navigation setup
    val navController = rememberNavController()
    val navScope = remember { NavigationScope() }

    // Feature registration
    val (featureEntries, uiConfigProviders) = getFeatures()

    // App configuration
    val appConfig = remember(navController, navScope, featureEntries, uiConfigProviders) {
        AppConfig(
            navController = navController,
            navScope = navScope,
            startDestination = DiscoverRoute,
            featureEntries = featureEntries,
            uiConfigProvider = uiConfigProviders
        )
    }

    // Dev client manager
    val devClientManager = remember { DevClientManager() }

    // Initialize heading observer
    LaunchedEffect(headingSource.heading) {
        headingSource.heading.collect { newValue ->
            GlobalState.setAngle(newValue)
        }
    }

    // Initialize native bridge and dev client
    LaunchedEffect(Unit) {
        NativeBridge.initLogger(RelayLogger)
        devClientManager.initializeFromDataStore(dataStore)
    }

    // App theme and container
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AppTheme {
            AppContainer(
                headingSource = headingSource,
                appConfig = appConfig,
                dataStore = dataStore,
                devClientManager = devClientManager
            )
        }
    }
}