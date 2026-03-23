package com.inumaki.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.InternalComposeApi
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.inumaki.core.ui.modifiers.shiningBorder
import com.inumaki.core.ui.theme.AppTheme
import kotlin.math.roundToInt

@Composable
fun SharedElementOverlay() {
    val controller = AppTheme.controller
    val transition = controller.active ?: return
    val end = transition.end ?: return

    // Animatable progress
    val progress = remember { Animatable(0f) }

    LaunchedEffect(transition) {
        progress.snapTo(0f)
        progress.animateTo(
            1f,
            animationSpec = spring(stiffness = Spring.StiffnessLow)
        )
        controller.finish(transition.key)
    }

    val midLeft = Offset(
        (transition.start.left + end.left) / 2f,
        (transition.start.top + end.top) / 2f - 100f // 100px above midpoint to create an arc
    )

    val animatedProgress by progress.asState()
    val rect = lerp(transition.start, end, animatedProgress)

    val topLeft = if (animatedProgress < 0.5f) {
        // first half: start -> mid
        lerp(
            Offset(transition.start.left, transition.start.top),
            midLeft,
            animatedProgress * 2f // scale 0..1
        )
    } else {
        // second half: mid -> end
        lerp(
            midLeft,
            Offset(end.left, end.top),
            (animatedProgress - 0.5f) * 2f
        )
    }

    val width = lerp(transition.start.width, end.width, animatedProgress)
    val height = lerp(transition.start.height, end.height, animatedProgress)

    val startRect = transition.start
    val scaleX = lerp(1f, end.width / startRect.width, animatedProgress)
    val scaleY = lerp(1f, end.height / startRect.height, animatedProgress)

    val density = LocalDensity.current

    Box(
        Modifier
            .fillMaxSize() // full screen overlay
    ) {
        Box(
            Modifier
                .absoluteOffset { IntOffset(topLeft.x.roundToInt(), topLeft.y.roundToInt()) }
                .size(
                    with(density) { width.toDp() },
                    with(density) { height.toDp() }
                )
                .shiningBorder(0f, 40.dp)
                .clip(RoundedCornerShape(40.dp))
                .background(AppTheme.colors.container)
        ) {
            // Start content fades out
            Box(
                Modifier
                    .alpha(1f - animatedProgress)
            ) {
                transition.startContent()
            }

            // End content fades in
            Box(
                Modifier
                    .offset(y = -(with(density) { end.top.toDp() }))
                    .alpha(animatedProgress)
            ) {
                transition.endContent?.invoke()
            }
        }
    }
}
