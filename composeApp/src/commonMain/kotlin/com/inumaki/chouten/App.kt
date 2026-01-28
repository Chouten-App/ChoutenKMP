package com.inumaki.chouten

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOut
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.inumaki.chouten.common.getFeatures
import com.inumaki.core.ui.AppScaffold
import com.inumaki.core.ui.components.SharedElement
import com.inumaki.core.ui.components.SharedElementOverlay
import com.inumaki.core.ui.model.AppConfig
import com.inumaki.core.ui.model.AppRoute
import com.inumaki.core.ui.model.DiscoverRoute
import com.inumaki.core.ui.model.FeatureEntry
import com.inumaki.core.ui.model.GlobalState
import com.inumaki.core.ui.model.HomeRoute
import com.inumaki.core.ui.model.NavigationScope
import com.inumaki.core.ui.model.PresentationStyle
import com.inumaki.core.ui.model.RepoRoute
import com.inumaki.core.ui.model.SettingsRoute
import com.inumaki.core.ui.model.presentationStyle
import com.inumaki.core.ui.theme.AppTheme
import com.inumaki.features.discover.DiscoverView
import com.inumaki.features.discover.DiscoverViewModel
import com.inumaki.features.home.HomeView
import com.inumaki.features.repo.RepoView
import dev.chouten.features.settings.SettingsView
import dev.chouten.features.settings.SettingsViewModel
import kotlinx.coroutines.flow.first
import kotlin.math.roundToInt

fun NavBackStackEntry.toAppRoute(featureEntries: List<FeatureEntry>): AppRoute? {
    return featureEntries
        .asSequence()
        .mapNotNull { it.tryCreateRoute(this) }
        .firstOrNull()
}

@Suppress("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun App(provider: HeadingSource, dataStore: DataStore<Preferences>) {
    val navController = rememberNavController()
    val (featureEntries, uiConfigProviders) = getFeatures()
    val backStackEntry by navController.currentBackStackEntryAsState()

    val backStackEntries by navController.currentBackStack.collectAsState()


    val currentRoute: AppRoute? = backStackEntry?.let { entry ->
        featureEntries
            .asSequence()
            .mapNotNull { feature -> feature.tryCreateRoute(entry) }
            .firstOrNull()
    }

    val fullscreenRoute = backStackEntries
        .mapNotNull { entry -> entry.toAppRoute(featureEntries) }
        .lastOrNull { route ->
            route.presentationStyle() == PresentationStyle.Fullscreen
        }

    val topRoute = backStackEntries
        .lastOrNull()
        ?.toAppRoute(featureEntries)

    val visible = topRoute?.presentationStyle() == PresentationStyle.Sheet

    val transition = updateTransition(
        targetState = visible,
        label = "ShowSheet"
    )

    val backgroundAlpha by transition.animateFloat(
        label = "BackgroundAlpha"
    ) { showSheet ->
        if (showSheet) 0.6f else 0f
    }

    val navScope = remember { NavigationScope() }
    val discoverVm = navScope.viewModelStore.get("discover") { DiscoverViewModel() }
    val settingsVm = navScope.viewModelStore.get("settings") { SettingsViewModel(dataStore) }

    LaunchedEffect(provider.heading) {
        provider.heading.collect { newValue ->
            GlobalState.setAngle(newValue)
        }
    }

    val appConfig = AppConfig(
        navController,
        navScope,
        DiscoverRoute,
        featureEntries,
        uiConfigProviders
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AppTheme {
            AppScaffold(
                provider.heading,
                appConfig
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    NavHost(
                        navController,
                        DiscoverRoute,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        composable<DiscoverRoute> { }
                        composable<HomeRoute> { }
                        composable<RepoRoute> { }
                        composable<SettingsRoute> { }
                    }

                    fullscreenRoute?.let { route ->
                        when (route) {
                            is DiscoverRoute -> DiscoverView(discoverVm)
                            is HomeRoute -> HomeView()
                            is RepoRoute -> RepoView()
                            else -> {}
                        }
                    }


                }
            }

            if (visible) {
                Box(
                    modifier = Modifier
                        .alpha(backgroundAlpha)
                        .fillMaxSize()
                        .background(Color.Black)
                        .clickable {
                            navController.popBackStack()
                        }
                )
            }



            BoxWithConstraints(
                modifier = Modifier.fillMaxSize()
            ) {
                CompositionLocalProvider(
                    LocalContentColor provides AppTheme.colors.fg
                ) {
                    topRoute?.let { route ->
                        when (route) {
                            is SettingsRoute -> {
                                SharedElement("settings_morph", offset = Offset(x = 0f, y = maxHeight.value * 0.3f)) {
                                    SettingsView(
                                        settingsVm,
                                        appConfig,
                                        modifier = Modifier
                                            .offset(y = maxHeight * 0.1f)
                                            .fillMaxWidth()
                                            .fillMaxHeight(0.9f)
                                    )
                                }
                            }

                            else -> {}
                        }
                    }

                    SharedElementOverlay()
                }
            }

            /*
            BoxWithConstraints(Modifier.fillMaxSize()) {
                val width by animateDpAsState(
                    targetValue = if (visible) maxWidth else 44.dp,
                    animationSpec = spring(
                        dampingRatio = 0.85f,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "sheetOffset"
                )
                val height by animateDpAsState(
                    targetValue = if (visible) (maxHeight.value * 0.9).dp else 44.dp,
                    animationSpec = spring(
                        dampingRatio = 0.85f,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "sheetOffset"
                )
                val offsetX by animateDpAsState(
                    targetValue = if (visible) 0.dp else 24.dp,
                    animationSpec = spring(
                        dampingRatio = 0.85f,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "sheetOffset"
                )
                val offsetY by animateDpAsState(
                    targetValue = if (visible) 0.dp else -46.dp,
                    animationSpec = spring(
                        dampingRatio = 0.85f,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "sheetOffset"
                )
                val cornerRadius by animateDpAsState(
                    targetValue = if (visible) 40.dp else 22.dp,
                    animationSpec = spring(
                        dampingRatio = 0.85f,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "sheetOffset"
                )

                CompositionLocalProvider(
                    LocalContentColor provides AppTheme.colors.fg
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .width(width)
                            .height(height)
                            .offset(x = offsetX, y = offsetY)
                            .clip(
                                RoundedCornerShape(cornerRadius)
                            )
                            .background(AppTheme.colors.container)
                    ) {
                        SettingsView(settingsVm, appConfig, expanded = visible)
                    }
                }
            }

             */
        }
    }
}