package com.inumaki.chouten.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.navigation.compose.rememberNavController
import chouten.composeapp.generated.resources.Res
import com.inumaki.chouten.HeadingSource
import com.inumaki.chouten.common.getFeatures
import com.inumaki.chouten.dev.DevClientManager
import com.inumaki.core.ui.model.AppConfig
import dev.chouten.core.repository.DefaultHostEnvironment
import com.inumaki.core.ui.model.DiscoverRoute
import com.inumaki.core.ui.model.GlobalState
import dev.chouten.core.repository.HostEnvironment
import com.inumaki.core.ui.model.NavigationScope
import dev.chouten.core.repository.SourceModule
import com.inumaki.core.ui.theme.AppTheme
import dev.chouten.core.repository.FileRepositoryStorage
import dev.chouten.core.repository.FileStore
import dev.chouten.core.repository.KtorRepositoryRemote
import dev.chouten.core.repository.ModuleManager
import dev.chouten.core.repository.RepositoryManager
import dev.chouten.core.repository.httpClient
import dev.chouten.runners.local.LocalRuntime
import dev.chouten.runners.relay.NativeBridge
import dev.chouten.runners.relay.RelayLogger
import dev.chouten.runners.relay.RelayRuntime
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.Resource


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
    val repositoryStorage = FileRepositoryStorage("repositories")
    val repositoryRemote = KtorRepositoryRemote(httpClient)

    val repositoryManager = RepositoryManager(
        repositoryStorage,
        repositoryRemote
    )

    val moduleManager = ModuleManager(
        repositoryManager = repositoryManager,
        runtimes = listOf(
            LocalRuntime(),
            RelayRuntime()
        )
    )

    val activeModule by moduleManager.activeModule.collectAsState()

    val runtime = remember { RelayRuntime() }

    // Initialize heading observer
    LaunchedEffect(headingSource.heading) {
        headingSource.heading.collect { newValue ->
            GlobalState.setAngle(newValue)
        }
    }

    // Initialize native bridge and dev client
    LaunchedEffect(Unit) {
        runtime.initialize(host = DefaultHostEnvironment())

        // Load module from files

        val sourceModule = SourceModule(
            "demo_module",
            binary = FileStore.read("repositories/demo_module.wasm")//Res.readBytes("files/demo_module.wasm")
        )
        runtime.load(sourceModule)

        //NativeBridge.initLogger(RelayLogger)
        //devClientManager.initializeFromDataStore(dataStore)
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
                devClientManager = devClientManager,
                runtime,
                repositoryManager,
                moduleManager
            )
        }
    }
}