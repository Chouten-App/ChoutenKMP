package com.inumaki.core.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import com.inumaki.core.ui.components.AppBottomBar
import com.inumaki.core.ui.components.AppButton
import com.inumaki.core.ui.components.AppTopBar
import com.inumaki.core.ui.components.alert.AlertHost
import com.inumaki.core.ui.components.alert.AlertManager
import com.inumaki.core.ui.model.AppConfig
import com.inumaki.core.ui.model.AppRoute
import com.inumaki.core.ui.model.FeatureEntry
import com.inumaki.core.ui.model.NavigationScope
import com.inumaki.core.ui.model.PresentationStyle
import com.inumaki.core.ui.model.ScrollGate
import com.inumaki.core.ui.model.SettingsRoute
import com.inumaki.core.ui.model.presentationStyle
import com.inumaki.core.ui.modifiers.shiningBorder
import com.inumaki.core.ui.theme.AppTheme
import com.inumaki.core.ui.theme.LocalAlertManager
import com.inumaki.core.ui.theme.LocalMatchedTransitionScope
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.let
import kotlin.math.roundToInt

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

data class TransitionState(
    val progress: Float = 0f,
    val isDragging: Boolean = false,
    val dragOffsetX: Float = 0f,
    val dragOffsetY: Float = 0f,
    val scaleX: Float = 0f,
    val scaleY: Float = 1f,
    val isCollapsing: Boolean = false,
    val collapseFromOffsetX: Float = 0f,
    val collapseFromOffsetY: Float = 0f,
    val collapseFromScaleY: Float = 1f,
    val collapseFromScaleX: Float = 1f,
)


data class TransitionGeometry(
    val x: Float,
    val y: Float,
    val width: Dp,
    val height: Dp,
    val topRadius: Dp,
    val bottomRadius: Dp,
    val scale: Float,
    val alpha: Float
)


class TransitionController(
    private val scope: CoroutineScope,
    private val progress: Animatable<Float, AnimationVector1D>
) {
    var state by mutableStateOf(TransitionState())
        private set


    var activeIdentifier by mutableStateOf<String?>(null)
        private set

    val isExpanded: Boolean
        get() = activeIdentifier != null

    private val collapseProgress = Animatable(0f)
    val collapseProgressValue: Float
        get() = collapseProgress.value

    suspend fun expand(identifier: String) {
        activeIdentifier = identifier
        if (!state.isDragging) {
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(500, easing = FastOutSlowInEasing)
            )
        }
    }

    suspend fun collapse() {
        // Capture where the sheet is right now
        val fromOffsetX = state.dragOffsetX
        val fromOffsetY = state.scaleX * -80f // mirror your translationY formula
        val fromScaleY = state.scaleY
        val fromScaleX = lerp(1f, 0.7f, state.scaleX)

        state = state.copy(
            isDragging = false,
            isCollapsing = true,
            collapseFromOffsetX = fromOffsetX,
            collapseFromOffsetY = fromOffsetY,
            collapseFromScaleY = fromScaleY,
            collapseFromScaleX = fromScaleX
        )

        collapseProgress.snapTo(0f)
        collapseProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(400, easing = FastOutSlowInEasing)
        )

        // Now hand back to normal progress animation
        state = state.copy(isCollapsing = false)
        progress.snapTo(0f)
        activeIdentifier = null
    }

    fun toggle(identifier: String) {
        if (activeIdentifier == identifier) {
            // Collapse
            scope.launch {
                if (!state.isDragging) {
                    progress.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(500, easing = FastOutSlowInEasing)
                    )
                }
                activeIdentifier = null  // Clear after animation
            }
        } else {
            // Expand - set identifier IMMEDIATELY before animation
            activeIdentifier = identifier
            scope.launch {
                if (!state.isDragging) {
                    progress.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(500, easing = FastOutSlowInEasing)
                    )
                }
            }
        }
    }

    fun updateDragState(
        isDragging: Boolean,
        dragOffsetX: Float = 0f,
        dragOffsetY: Float = 0f,
        scaleX: Float = 0f,
        scaleY: Float = 1f
    ) {
        state = state.copy(
            isDragging = isDragging,
            dragOffsetX = dragOffsetX,
            dragOffsetY = dragOffsetY,
            scaleX = scaleX,
            scaleY = scaleY
        )
    }

    fun snapProgress(value: Float) {
        scope.launch {
            progress.snapTo(value.coerceIn(0f, 1f))
        }
    }

    fun getCurrentProgress(): Float = progress.value

}


@Composable
fun rememberTransitionController(): TransitionController {
    val scope = rememberCoroutineScope()
    val progress = remember { Animatable(0f) }
    return remember { TransitionController(scope, progress) }
}


// Geometry calculator (same as before)
class TransitionGeometryCalculator(
    private val collapsedSize: Dp = 48.dp,
    private val midSize: Dp = 200.dp,
    private val maxRadius: Dp = 60.dp,
    private val finalRadius: Dp = 34.dp
) {
    private fun quadBezier(p0: Float, p1: Float, p2: Float, t: Float): Float {
        val u = 1f - t
        return u * u * p0 + 2f * u * t * p1 + t * t * p2
    }


    private fun phaseT(t: Float, start: Float, end: Float): Float =
        ((t - start) / (end - start)).coerceIn(0f, 1f)

    fun Alignment.toUnitOffset(): Offset {
        val biasAlignment = this as? BiasAlignment
            ?: throw IllegalArgumentException("Alignment must be BiasAlignment")

        return Offset(
            x = (biasAlignment.horizontalBias + 1f) / 2f,
            y = (biasAlignment.verticalBias + 1f) / 2f
        )
    }

    // Update the geometry calculator
    fun calculate(
        t: Float,
        maxWidth: Dp,
        maxHeight: Dp,
        startPosition: Offset = Offset.Zero,
        startSize: IntSize = IntSize.Zero,
        alignment: Alignment = Alignment.BottomStart,
        containerWidth: Float,
        containerHeight: Float
    ): TransitionGeometry {
        val alignmentOffset = alignment.toUnitOffset()

        val startX = startPosition.x - containerWidth * alignmentOffset.x
        val startY = startPosition.y - containerHeight * alignmentOffset.y + startSize.height// containerHeight - startPosition.y - startSize.height

        val targetX = 0f
        val targetY = 0f

        //val x = lerp(startX, targetX, t)
        //val y = lerp(startY, targetY, t)
        //val t = progress.value

        val middleValue = if (startPosition.y < maxHeight.value / 2) 400.dp.value else -1200.dp.value

        val x = quadBezier( p0 = startX, p1 = 0f, p2 = targetX, t = t )
        val y = quadBezier( p0 = startY, p1 = middleValue, p2 = targetY, t = t )

        val density = containerWidth / maxWidth.value // Approximate density
        val startWidthDp = (startSize.width / density).dp
        val startHeightDp = (startSize.height / density).dp

        val cornerPhaseStart = 0.52f
        val baseRadius = if (t < cornerPhaseStart) {
            lerp(
                startHeightDp / 2,
                maxRadius,
                FastOutSlowInEasing.transform(phaseT(t, 0f, cornerPhaseStart))
            )
        } else {
            maxRadius
        }

        val settleT = phaseT(t, cornerPhaseStart, 1f)

        val topRadius = if (t < cornerPhaseStart) {
            baseRadius
        } else {
            lerp(maxRadius, finalRadius, LinearOutSlowInEasing.transform(settleT))
        }

        val bottomRadius = if (t < cornerPhaseStart) {
            baseRadius
        } else {
            lerp(maxRadius, finalRadius, LinearOutSlowInEasing.transform(settleT))
        }

        val width = when {
            t < 0.5f -> lerp(
                startWidthDp,
                midSize,
                FastOutSlowInEasing.transform(phaseT(t, 0f, 0.5f))
            )
            else -> lerp(
                midSize,
                maxWidth,
                LinearOutSlowInEasing.transform(phaseT(t, 0.5f, 1f))
            )
        }

        val expandedHeight = maxHeight * 0.9f
        val height = when {
            t < 0.5f -> lerp(
                startHeightDp,
                midSize,
                FastOutSlowInEasing.transform(phaseT(t, 0f, 0.5f))
            )
            else -> lerp(
                midSize,
                expandedHeight,
                LinearOutSlowInEasing.transform(phaseT(t, 0.5f, 1f))
            )
        }

        val scale = when {
            t < 0.5f -> FastOutSlowInEasing.transform(phaseT(t, 0f, 0.16f))
            else -> lerp(
                0.16f,
                1f,
                LinearOutSlowInEasing.transform(phaseT(t, 0.16f, 1f))
            )
        }

        return TransitionGeometry(
            x = x,
            y = y,
            width = width,
            height = height,
            topRadius = topRadius,
            bottomRadius = bottomRadius,
            scale = scale,
            alpha = t
        )
    }

}


// Matched transition scope
interface MatchedTransitionScope {
    @Composable
    fun MatchedElement(
        identifier: String,
        modifier: Modifier,
        alignment: Alignment,
        content: @Composable BoxScope.() -> Unit
    )

    @Composable
    fun Sheet(
        identifier: String,
        content: @Composable BoxScope.() -> Unit
    )

}


@Composable
fun MatchedElement(
    identifier: String,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    content: @Composable BoxScope.() -> Unit
) {
    LocalMatchedTransitionScope.current.MatchedElement(
        identifier, modifier, alignment, content
    )
}


// Internal data for sheets
data class SheetContent(
    val identifier: String,
    val sourceContent: (@Composable BoxScope.() -> Unit)? = null,
    val expandedContent: @Composable BoxScope.() -> Unit,
    val sourcePosition: Offset = Offset.Zero,
    val sourceSize: IntSize = IntSize.Zero,
    val alignment: Alignment
)

class MatchedTransitionScopeImpl(
    private val controller: TransitionController,
    private val sheets: MutableMap<String, SheetContent>
) : MatchedTransitionScope {

    @Composable
    override fun MatchedElement(
        identifier: String,
        modifier: Modifier,
        alignment: Alignment,
        content: @Composable BoxScope.() -> Unit
    ) {
        var globalPosition by remember { mutableStateOf(Offset.Zero) }
        var size by remember { mutableStateOf(IntSize.Zero) }

        DisposableEffect(identifier, globalPosition, size) {
            sheets[identifier]?.let { existing ->
                sheets[identifier] = existing.copy(
                    sourceContent = content,
                    sourcePosition = globalPosition,
                    sourceSize = size,
                    alignment = Alignment.TopStart
                )
            } ?: run {
                sheets[identifier] = SheetContent(
                    identifier = identifier,
                    sourceContent = content,
                    expandedContent = {},
                    sourcePosition = globalPosition,
                    sourceSize = size,
                    alignment = alignment
                )
            }
            onDispose { }
        }

        val isActive = controller.activeIdentifier == identifier

        Box(
            modifier = modifier
                .onGloballyPositioned { coordinates ->
                    globalPosition = coordinates.positionInRoot()
                    size = coordinates.size
                }
                .alpha(if (isActive) 0f else 1f)
                .clip(RoundedCornerShape(50))
                .clickable { controller.toggle(identifier) }
                //.shiningBorder(60f, 40.dp)
        ) {
            content()
        }
    }

    @Composable
    override fun Sheet(
        identifier: String,
        content: @Composable BoxScope.() -> Unit
    ) {
        DisposableEffect(identifier) {
            sheets[identifier]?.let { existing ->
                sheets[identifier] = existing.copy(
                    expandedContent = content
                )
            } ?: run {
                sheets[identifier] = SheetContent(
                    identifier = identifier,
                    sourceContent = null,
                    expandedContent = content,
                    sourcePosition = Offset.Zero,
                    sourceSize = IntSize.Zero,
                    alignment = Alignment.BottomStart
                )
            }

            onDispose {
                sheets.remove(identifier)
            }
        }
    }
}

// Main scaffold
@Composable
fun AppScaffold(
    controller: TransitionController = rememberTransitionController(),
    appConfig: AppConfig,
    geometryCalculator: TransitionGeometryCalculator = remember { TransitionGeometryCalculator() },
    backgroundColor: Color = Color(0xff0c0c0c),
    containerColor: Color = Color(0xff171717),
    borderColor: Color = Color(0xff3b3b3b),
    content: @Composable MatchedTransitionScope.() -> Unit
) {
    val sheets = remember { mutableStateMapOf<String, SheetContent>() }

    val manager = remember { AlertManager() }

    val coroutineScope = rememberCoroutineScope()
    val scope = remember(controller) {
        MatchedTransitionScopeImpl(controller, sheets)
    }

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

    fun segment(t: Float, start: Float, end: Float): Float {
        return ((t - start) / (end - start)).coerceIn(0f, 1f)
    }




    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        val maxWidth = this.maxWidth
        val maxHeight = this.maxHeight

        // Main content
        CompositionLocalProvider(
            LocalMatchedTransitionScope provides scope,
            LocalAlertManager provides manager,
            LocalContentColor provides AppTheme.colors.fg
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                content(scope)
            }

            AppTopBar(topConfig, 0f, modifier = Modifier.align(Alignment.TopCenter))
            AppBottomBar(0f, appConfig.navController, modifier = Modifier.align(Alignment.BottomCenter))
        }

        // Sheet overlay (if active)
        controller.activeIdentifier?.let { activeId ->
            val t = controller.getCurrentProgress()
            val activeSheet = sheets[activeId]

            val isCollapsing = controller.state.isCollapsing
            val collapseProgress = controller.collapseProgressValue

            val sizeScaleX = when {
                isCollapsing -> lerp(controller.state.collapseFromScaleX, 0f, collapseProgress)
                else -> lerp(1f, 0.7f, controller.state.scaleX)
            }
            val sizeScaleY = when {
                isCollapsing -> lerp(controller.state.collapseFromScaleY, 0f, collapseProgress)
                else -> controller.state.scaleY
            }

            if (activeSheet != null) {
                val geometry = geometryCalculator.calculate(
                    t = t,
                    maxWidth = maxWidth,
                    maxHeight = maxHeight,
                    startPosition = activeSheet.sourcePosition,
                    startSize = activeSheet.sourceSize,
                    containerWidth = maxWidth.value * LocalDensity.current.density,
                    containerHeight = maxHeight.value * LocalDensity.current.density,
                    //alignment = activeSheet.alignment
                )
                CompositionLocalProvider(
                    LocalMatchedTransitionScope provides scope,
                    LocalContentColor provides AppTheme.colors.fg
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .graphicsLayer {
                                translationX = when {
                                    isCollapsing -> lerp(controller.state.collapseFromOffsetX, activeSheet.sourcePosition.x, collapseProgress)
                                    controller.state.isDragging -> geometry.x + controller.state.dragOffsetX
                                    else -> geometry.x
                                }
                                translationY = when {
                                    isCollapsing -> lerp(controller.state.collapseFromOffsetY, activeSheet.sourcePosition.y, collapseProgress)
                                    controller.state.isDragging -> geometry.y + controller.state.scaleX * -80f
                                    else -> geometry.y
                                }
                            }
                            .width(
                                geometry.width * sizeScaleX
                            )
                            .height(geometry.height * sizeScaleY)
                            .clip(
                                RoundedCornerShape(
                                    topStart = geometry.topRadius,
                                    topEnd = geometry.topRadius,
                                    bottomStart = geometry.bottomRadius,
                                    bottomEnd = geometry.bottomRadius
                                )
                            )
                            .background(containerColor)
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = {
                                        if (!ScrollGate.canDrag) return@detectDragGestures
                                        controller.updateDragState(
                                            isDragging = true,
                                            dragOffsetX = 0f,
                                            dragOffsetY = 0f  // reset raw delta on each drag start
                                        )
                                    },
                                    onDrag = { change, dragAmount ->
                                        if (!ScrollGate.canDrag) return@detectDragGestures
                                        change.consume()

                                        // accumulate raw finger delta only
                                        val newOffsetY = controller.state.dragOffsetY + dragAmount.y
                                        val newOffsetX = controller.state.dragOffsetX + dragAmount.x
                                        val maxDragDistanceY = maxHeight.toPx() * 0.6f
                                        val maxDragDistanceX = maxWidth.toPx()
                                        val clampedY = (newOffsetY / maxDragDistanceY).coerceIn(0f, 1f)
                                        val clampedX = (newOffsetX / maxDragDistanceX).coerceIn(0f, 1f)

                                        controller.updateDragState(
                                            isDragging = true,
                                            dragOffsetX = newOffsetX,  // store raw delta, not scaled
                                            dragOffsetY = newOffsetY,
                                            scaleX = lerp(0f, 1f, clampedY),
                                            scaleY = lerp(1f, 0.3f, clampedY)
                                        )
                                    },
                                    onDragEnd = {
                                        println("Controller State -> ${controller.state}")
                                        if (controller.state.scaleY < 0.55f) {
                                            //controller.snapProgress(1f)
                                            coroutineScope.launch { controller.collapse() }
                                            controller.updateDragState(isDragging = false)
                                        } else {
                                            coroutineScope.launch { controller.expand(activeId) }
                                            controller.updateDragState(isDragging = false)
                                        }
                                    },
                                    onDragCancel = {
                                        if (controller.state.scaleY < 0.55f) {
                                            //controller.snapProgress(1f)
                                            controller.updateDragState(isDragging = false)
                                            coroutineScope.launch { controller.collapse() }
                                        } else {
                                            controller.updateDragState(isDragging = false)
                                            coroutineScope.launch { controller.expand(activeId) }
                                        }
                                    }
                                )
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .graphicsLayer {
                                    alpha = segment(geometry.alpha, 0.5f, 1f)
                                }
                                .fillMaxSize()
                        ) {
                            val widthScale = lerp(1f, 0.7f, controller.state.scaleX)
                            val heightScale = controller.state.scaleY
                            val contentScale = maxOf(widthScale, heightScale)

                            Box(
                                modifier = Modifier
                                    .requiredSize(geometry.width, geometry.height)
                                    .align(Alignment.TopCenter)
                                    .graphicsLayer {
                                        scaleX = contentScale
                                        scaleY = contentScale
                                        transformOrigin = TransformOrigin(0.5f, 0f)
                                    }
                            ) {
                                activeSheet.expandedContent(this)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .graphicsLayer {
                                    scaleX = 1 + geometry.scale
                                    scaleY = 1 + geometry.scale
                                    alpha = 1f - segment(geometry.alpha, 0f, 0.5f)
                                    transformOrigin = TransformOrigin(0.5f, 0f)
                                }
                                .align(Alignment.TopCenter)
                        ) {
                            activeSheet.sourceContent?.let { it(this) }
                        }
                    }
                }
            }
        }

        // alert
        AlertHost(manager)

    }
}


@Suppress("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AppScaffoldOld(
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

