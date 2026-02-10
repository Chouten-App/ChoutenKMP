package com.inumaki.core.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import com.inumaki.core.ui.components.AppBottomBar
import com.inumaki.core.ui.components.AppButton
import com.inumaki.core.ui.components.AppTopBar
import com.inumaki.core.ui.model.AppConfig
import com.inumaki.core.ui.model.AppRoute
import com.inumaki.core.ui.model.FeatureEntry
import com.inumaki.core.ui.model.NavigationScope
import com.inumaki.core.ui.model.PresentationStyle
import com.inumaki.core.ui.model.SettingsRoute
import com.inumaki.core.ui.model.presentationStyle
import com.inumaki.core.ui.theme.AppTheme
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

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

@Suppress("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AppScaffold(
    heading: StateFlow<Float>,
    appConfig: AppConfig,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
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

    Scaffold(
        topBar = { AppTopBar(topConfig, angle.value) },
        bottomBar = { AppBottomBar(angle.value, appConfig.navController) },
        containerColor = AppTheme.colors.background,
        contentColor = AppTheme.colors.fg,
        modifier = modifier.fillMaxSize()
    ) {  padding ->
        content()
    }
}

