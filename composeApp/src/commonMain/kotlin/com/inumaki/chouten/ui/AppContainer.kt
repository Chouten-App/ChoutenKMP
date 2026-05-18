package com.inumaki.chouten.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import chouten.composeapp.generated.resources.Res
import coil3.compose.AsyncImage
import com.inumaki.chouten.HeadingSource
import com.inumaki.chouten.dev.DevClientManager
import com.inumaki.chouten.navigation.AppNavHost
import com.inumaki.chouten.navigation.NavigationState
import com.inumaki.chouten.navigation.overlay.FullscreenOverlay
import com.inumaki.chouten.navigation.overlay.SheetOverlay
import com.inumaki.chouten.navigation.overlay.SheetScrim
import com.inumaki.core.ui.AppScaffold
import com.inumaki.core.ui.components.AppAsyncImage
import com.inumaki.core.ui.components.AppButton
import com.inumaki.core.ui.components.AppImage
import com.inumaki.core.ui.components.AppImageButton
import com.inumaki.core.ui.components.SharedElementOverlay
import com.inumaki.core.ui.model.AppConfig
import com.inumaki.core.ui.model.PresentationStyle
import dev.chouten.core.repository.Runtime
import com.inumaki.core.ui.model.presentationStyle
import com.inumaki.core.ui.modifiers.shiningBorder
import com.inumaki.core.ui.rememberTransitionController
import com.inumaki.core.ui.theme.AppTheme
import dev.chouten.core.repository.InstalledModule
import dev.chouten.core.repository.ModuleManager
import dev.chouten.core.repository.RemoteModule
import dev.chouten.core.repository.RepositoryManager
import dev.chouten.features.settings.SettingsView
import dev.chouten.features.settings.SettingsViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

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
    runtime: Runtime,
    repositoryManager: RepositoryManager,
    moduleManager: ModuleManager,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    // Observe navigation state
    val backStackEntries by appConfig.navController.currentBackStack.collectAsState()

    val navigationState = remember(backStackEntries, appConfig.featureEntries) {
        NavigationState.from(backStackEntries, appConfig.featureEntries)
    }

    //val showSheetOverlay = navigationState.topRoute?.presentationStyle() == PresentationStyle.Sheet
    val controller = rememberTransitionController()
    var allModules by remember { mutableStateOf<List<Pair<InstalledModule?, RemoteModule>>>(emptyList()) }

    val activeModule by moduleManager.activeModule.collectAsState()

    LaunchedEffect(Unit) {
        repositoryManager.refreshRepositories()
        allModules = repositoryManager.getAllModules()
    }

    Box(modifier = modifier.fillMaxSize()) {
        AppScaffold(controller = controller, appConfig) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Primary navigation host
                AppNavHost(appConfig = appConfig)

                navigationState.fullscreenRoute?.let { route ->
                    FullscreenOverlay(
                        route = route,
                        navScope = appConfig.navScope,
                        devClientManager,
                        runtime,
                        repositoryManager
                    )
                }
            }


            Sheet(identifier = "profileDetails") {
                val viewModel = appConfig.navScope.viewModelStore.get("settings") {
                    SettingsViewModel(
                        dataStore = dataStore,
                        onCliChange = { cliIP ->
                            devClientManager.initialize(cliIP)
                        }
                    )
                }
                SettingsView(viewModel, appConfig)

                // Close button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppButton(
                        "drawable/xmark-solid-full.svg",
                        0f,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable {
                                controller.toggle("profileDetails")
                            },
                        background = AppTheme.colors.overlay
                    )

                    Text(
                        "Details",
                        style = TextStyle(
                            color = Color(0xffd7d7d7),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(modifier = Modifier.size(44.dp))
                }
            }

            Sheet("ModuleSelector") {
                LazyColumn(
                    modifier = Modifier
                        .padding(top = 50.dp)
                        .padding(20.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(AppTheme.colors.overlay)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    itemsIndexed(allModules) { index, (local, remote) ->
                        val moduleState = if (local == null) "GET" else if (local.version != remote.version) "UPDATE" else "REFRESH"
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val iconPath = local?.imagePath ?: remote.iconUrl
                                    iconPath?.let {
                                        AppAsyncImage(
                                            it,
                                            modifier = Modifier
                                                .width(44.dp)
                                                .height(44.dp)
                                                .border(if (activeModule?.id == local?.id) 1.dp else 0.dp, AppTheme.colors.accent, RoundedCornerShape(8.dp))
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable {
                                                    local?.let { module ->
                                                        scope.launch {
                                                            moduleManager.loadModule(module.id)
                                                            moduleManager.activateModule(module.id)
                                                        }
                                                    }
                                                }
                                        )
                                    }
                                    Column {
                                        Text(remote.name)
                                        Text(remote.author, modifier = Modifier.alpha(0.7f))
                                    }
                                }

                                Text(
                                    moduleState,
                                    style = TextStyle(
                                        color = AppTheme.colors.accent,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(AppTheme.colors.border)
                                        .padding(8.dp, 4.dp)
                                        .clickable {
                                            when (moduleState) {
                                                "GET" -> {
                                                    println("Installing ${remote.name}")

                                                    scope.launch {
                                                        repositoryManager.installModule(remote.id)
                                                        allModules = repositoryManager.getAllModules()
                                                    }
                                                }
                                                "REFRESH" -> {
                                                    println("Refreshing ${remote.name}")

                                                    scope.launch {
                                                        repositoryManager.installModule(remote.id)
                                                        allModules = repositoryManager.getAllModules()
                                                    }
                                                }
                                                "UPDATE" -> {

                                                }
                                            }

                                        }
                                )
                            }

                            if (index < allModules.size - 1) {
                                HorizontalDivider(
                                    Modifier.padding(start = 52.dp, top = 14.dp),
                                    color = AppTheme.colors.border,
                                    thickness = 1.dp
                                )
                            }
                        }
                    }
                }

                // Close button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppButton(
                        "drawable/xmark-solid-full.svg",
                        0f,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable {
                                controller.toggle("ModuleSelector")
                            },
                        background = AppTheme.colors.overlay
                    )

                    Text(
                        "Modules",
                        style = TextStyle(
                            color = Color(0xffd7d7d7),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(modifier = Modifier.size(44.dp))
                }
            }
        }
        // Main scaffold with navigation
        /*
        AppScaffold(
            headingSource.heading,
            appConfig
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Primary navigation host
                AppNavHost(appConfig = appConfig)

                navigationState.fullscreenRoute?.let { route ->
                    FullscreenOverlay(
                        route = route,
                        navScope = appConfig.navScope,
                        devClientManager,
                        runtime,
                        repositoryManager
                    )
                }
            }
        }
         */

        /*
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


        Box(
            modifier
                .fillMaxSize()
                .background(Color.Black.copy(0.5f))
        )
        // Alert
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(52.dp)
                .fillMaxWidth()
                .shiningBorder(60f, 32.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(AppTheme.colors.background)
                .padding(14.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(start = 10.dp, end = 10.dp, top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Add Repository",
                        style = TextStyle(
                            fontWeight = FontWeight.SemiBold,
                            color = AppTheme.colors.fg,
                            fontSize = 18.sp
                        )
                    )
                    Text(
                        "Enter the url to the repositories json file",
                        style = TextStyle(color = AppTheme.colors.fg.copy(0.7f))
                    )
                }

                BasicTextField(
                    value = "Repository URL",
                    onValueChange = {
                        println("Url: $it")
                    },
                    cursorBrush = SolidColor(AppTheme.colors.accent),
                    textStyle = TextStyle(color = AppTheme.colors.fg.copy(0.5f)),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(50))
                                .background(AppTheme.colors.overlay)
                                .padding(12.dp)
                        ) {
                            innerTextField()
                        }
                    }
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        "Cancel",
                        style = TextStyle(fontWeight = FontWeight.SemiBold, color = AppTheme.colors.fg, fontSize = 16.sp),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(50))
                            .background(AppTheme.colors.overlay)
                            .padding(14.dp)
                    )
                    Text(
                        "OK",
                        style = TextStyle(fontWeight = FontWeight.SemiBold, color = AppTheme.colors.fg, fontSize = 16.sp),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(50))
                            .background(AppTheme.colors.overlay)
                            .padding(14.dp)
                    )
                }
            }
        }

         */
    }
}