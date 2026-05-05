package com.inumaki.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.navigation.NavHostController
import chouten.core.ui.generated.resources.Res
import coil3.compose.AsyncImage
import com.inumaki.core.ui.MatchedElement
import com.inumaki.core.ui.model.DiscoverRoute
import com.inumaki.core.ui.model.HomeRoute
import com.inumaki.core.ui.model.RepoRoute
import com.inumaki.core.ui.model.SettingsRoute
import com.inumaki.core.ui.modifiers.shiningBorder
import com.inumaki.core.ui.theme.AppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Suppress("UnusedBoxWithConstraintsScope")
@Composable
fun AppBottomBar(angle: Float, navController: NavHostController, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()

    val items = listOf(
        "Home" to "drawable/house-solid-full.svg",
        "Discover" to "drawable/compass-solid-full.svg",
        "Repo" to "drawable/box-open-solid-full.svg"
    )

    var selectedIndex by remember { mutableStateOf(1) }
    var searching by remember { mutableStateOf(false) }
    var isDragging by remember { mutableStateOf(false) }
    var didTap by remember { mutableStateOf(false) }
    var showProfile by remember { mutableStateOf(false) }
    val barHeight = AppTheme.layout.bottomBarItemSize.height
    val controller = AppTheme.controller
    val circleSize = 42.dp

    val velocityTracker = remember { VelocityTracker() }

    val transition = updateTransition(
        targetState = searching,
        label = "SearchTransition"
    )
    val draggingTransition = updateTransition(
        targetState = isDragging,
        label = "DraggingTransition"
    )

    var dragOffset by remember { mutableStateOf(0.dp) }


    val containerCorner by transition.animateDp(
        label = "ContainerCorner"
    ) { isSearching ->
        if (isSearching) barHeight else 50.dp
    }



    val indicatorScale by draggingTransition.animateFloat(
        label = "IndicatorScale"
    ) { isDragging ->
        if (isDragging) 1.3f else if (didTap) 1.15f else 1f
    }

    val barScale by draggingTransition.animateFloat(
        label = "IndicatorScale"
    ) { isDragging ->
        if (isDragging || didTap) 1.01f else 1f
    }

    var interactionVelocity = remember { Animatable(0f) }
    var velocity by remember { mutableStateOf(0f) }

    val combinedVelocity = velocity + interactionVelocity.value

    val smoothedVelocity by animateFloatAsState(
        targetValue = combinedVelocity,
        animationSpec = spring(stiffness = 400f),
        label = "VelocitySmoothing"
    )
    val maxVelocity = 800f // tune
    val velocityFactor = (abs(smoothedVelocity) / maxVelocity).coerceIn(0f, 1f)

    val shaped = velocityFactor * velocityFactor

    val scaleXVel = 1f - shaped * 0.3f
    val scaleYVel = 1f + shaped * 0.3f

    val indicatorAlpha by transition.animateFloat(
        label = "IndicatorAlpha"
    ) { isSearching ->
        if (isSearching) 0f else 1f
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    0.0f to AppTheme.colors.background.copy(0f),
                    1.0f to AppTheme.colors.background,
                )
            )
    ) {
        val circleWidth by transition.animateDp(
            label = "CircleWidth"
        ) { isSearching ->
            if (isSearching) 0.dp else 56.dp
        }

        val itemWidth = (maxWidth - 112.dp - 48.dp - 16.dp - 8.dp) / 3
        fun finalizeIndex() {
            val rawIndex = (selectedIndex * itemWidth + dragOffset) / itemWidth
            selectedIndex = rawIndex.roundToInt().coerceIn(0, 2)
            dragOffset = 0.dp

            when (selectedIndex) {
                0 -> navController.navigate(HomeRoute)
                1 -> navController.navigate(DiscoverRoute)
                2 -> navController.navigate(RepoRoute)
                else -> {}
            }
        }

        val containerWidth by transition.animateDp(
            label = "ContainerWidth"
        ) { isSearching ->
            if (isSearching) barHeight else itemWidth
        }

        val animatedOffset by animateDpAsState(
            targetValue = itemWidth * selectedIndex + dragOffset,
            label = "BottomBarIndicator"
        )
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(
                    start = AppTheme.layout.screenEdgePadding.calculateLeftPadding(LayoutDirection.Ltr),
                    bottom = AppTheme.layout.screenEdgePadding.calculateBottomPadding(),
                    end = AppTheme.layout.screenEdgePadding.calculateRightPadding(LayoutDirection.Ltr)
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            if (circleWidth > 0.5.dp ) {
                MatchedElement(
                    "profileDetails",
                    modifier = Modifier,
                    alignment = Alignment.BottomStart,
                ) {
                    AppImageButton(
                        "https://i.pinimg.com/1200x/7b/1d/dc/7b1ddcab5e7fccfb8a00ca680f4a24c3.jpg",
                        angle,
                        circleWidth,
                        50.dp,
                    )
                }
            }
            AnimatedVisibility(
                visible = !searching,
                enter = expandHorizontally(
                    expandFrom = Alignment.Start
                ),
                exit = shrinkHorizontally(
                    shrinkTowards = Alignment.Start
                )
            ) {
                Spacer(modifier = Modifier.width(8.dp))
            }

            Box(
                modifier = Modifier
                    .scale(barScale)
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragStart = {
                                scope.launch {
                                    interactionVelocity.stop()
                                    interactionVelocity.snapTo(0f)
                                }
                                velocityTracker.resetTracking()
                                isDragging = true
                            },
                            onDragEnd = {
                                velocity = velocityTracker.calculateVelocity().x
                                finalizeIndex()
                                velocity = 0f
                                isDragging = false
                            },
                            onDragCancel = {
                                velocityTracker.resetTracking()
                                dragOffset = 0.dp
                                isDragging = false
                            },
                            onHorizontalDrag = { change, dragAmount ->
                                velocityTracker.addPosition(
                                    change.uptimeMillis,
                                    change.position
                                )
                                val minX = 0.dp
                                val maxX = itemWidth * 2

                                val currentX = selectedIndex * itemWidth + dragOffset
                                val nextX = currentX + dragAmount.toDp()
                                val clampedX = nextX.coerceIn(minX, maxX)

                                dragOffset = clampedX - (selectedIndex * itemWidth)
                                change.consume()
                                velocity = velocityTracker.calculateVelocity().x.coerceIn(-maxVelocity, maxVelocity)
                                println("Velocity -> $velocity")
                            }
                        )
                    }
            ) {
                Box(
                    modifier = Modifier
                        .padding(if (searching) 4.dp else 0.dp)
                        .width(if (searching) barHeight + 8.dp else itemWidth * 3 + 8.dp)
                        .height(
                            barHeight + 8.dp
                        )
                        .shiningBorder(
                            angle, (barHeight + 8.dp) / 2
                        )
                        .clip(RoundedCornerShape(50))
                        .background(AppTheme.colors.container)
                        .animateContentSize( )
                        .padding(4.dp)
                )

                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            transformOrigin = TransformOrigin.Center
                            scaleX = scaleXVel * indicatorScale
                            scaleY = scaleYVel * indicatorScale
                            translationX = animatedOffset.toPx()
                        }
                        .padding(4.dp)
                        .alpha(indicatorAlpha)
                        //.offset(x = animatedOffset)
                        //.scale(indicatorScale)
                        .width(containerWidth)
                        .height(barHeight)
                        .clip(RoundedCornerShape(50))
                        .background(AppTheme.colors.overlay)
                )

                Row(
                    modifier = Modifier
                        .height(barHeight)
                        .padding(4.dp)
                ) {
                    items.forEachIndexed { index, (title, icon) ->

                        val shouldBeVisible =
                            !searching || (searching && index == selectedIndex)


                        val itemAlpha by animateFloatAsState(
                            targetValue = if (shouldBeVisible) 1f else 0f,
                            label = "ItemAlpha"
                        )

                        val itemWidthAnim by animateDpAsState(
                            targetValue = if (shouldBeVisible && searching) barHeight + 8.dp else if (shouldBeVisible) itemWidth else 0.dp,
                            label = "ItemWidth"
                        )

                        AppBottomBarItem(
                            title = title,
                            icon = icon,
                            isSelected = selectedIndex == index,
                            showLabel = !searching,
                            modifier = Modifier
                                .width(itemWidthAnim)
                                .alpha(itemAlpha)
                                .clickable(
                                    enabled = shouldBeVisible, // important
                                    indication = null,
                                    interactionSource = null
                                ) {
                                    didTap = true
                                    selectedIndex = index
                                    val impulse = if (index > selectedIndex) maxVelocity else -maxVelocity

                                    scope.launch {
                                        interactionVelocity.stop()
                                        interactionVelocity.snapTo(impulse)
                                        interactionVelocity.animateTo(
                                            targetValue = 0f,
                                            animationSpec = tween(durationMillis = 700)
                                        )
                                    }
                                    scope.launch {
                                        delay(300)
                                        didTap = false
                                    }
                                    searching = false
                                    isDragging = false
                                    when (selectedIndex) {
                                        0 -> navController.navigate(HomeRoute)
                                        1 -> navController.navigate(DiscoverRoute)
                                        2 -> navController.navigate(RepoRoute)
                                        else -> {}
                                    }
                                }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Search button / field
            Row(
                modifier = Modifier
                    .height(56.dp)
                    .shiningBorder(angle, 50.dp)
                    .clip(RoundedCornerShape(50))
                    .background(AppTheme.colors.container)
                    .animateContentSize( )
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        searching = true
                    },
                horizontalArrangement = Arrangement.spacedBy((-6).dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                        Res.getUri("drawable/magnifying-glass-solid-full.svg"),
                    contentDescription = "",
                    colorFilter = ColorFilter.tint(Color(0xffd3d3d3), BlendMode.SrcIn),
                    modifier = Modifier
                        .padding(16.dp)
                        .width(56.dp - 32.dp)
                        .aspectRatio(1f)
                        .alpha(if (searching) 1f else 0.7f)
                )

                if (searching) {
                    Text("Search...", fontSize = 14.sp, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}