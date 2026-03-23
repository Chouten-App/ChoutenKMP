package com.inumaki.core.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.center
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toOffset
import com.inumaki.core.ui.modifiers.shiningBorder
import com.inumaki.core.ui.theme.AppTheme
import kotlinx.coroutines.launch

@Composable
fun AppTintedButton(title: String, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    val animatedTouch = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    var isDragging by remember { mutableStateOf(false) }

    val buttonAlpha by animateFloatAsState(
        targetValue = if (isDragging) 1f else 0f,
        animationSpec = tween(300),
        label = "alpha"
    )
    val buttonScale by animateFloatAsState(
        targetValue = if (isDragging) 1.05f else 1f,
        animationSpec = tween(300),
        label = "alpha"
    )
    val buttonParallax = animatedTouch.value * 0.06f

    Box(
        modifier = modifier
            .offset {
                IntOffset(
                    buttonParallax.x.toInt(),
                    buttonParallax.y.toInt()
                )
            }
            .scale(buttonScale)
            .height(44.dp)
            .shiningBorder(0f, 50.dp, AppTheme.colors.accent, AppTheme.colors.accentBorder)
            .clip(RoundedCornerShape(50))
            .background(AppTheme.colors.accent)
            .pointerInput(Unit) {
                detectDragGestures(

                    onDragStart = {
                        isDragging = true
                    },

                    onDrag = { change, _ ->
                        val center = size.center.toOffset()
                        val relative = change.position - center
                        println("Relative -> $relative")

                        scope.launch {
                            animatedTouch.animateTo(
                                relative,
                                spring(
                                    dampingRatio = 0.7f,      // elasticity
                                    stiffness = 900f          // tightness
                                )
                            )
                        }
                    },

                    onDragEnd = {
                        isDragging = false
                        scope.launch {
                            animatedTouch.animateTo(
                                Offset.Zero,
                                spring(
                                    dampingRatio = 0.6f,
                                    stiffness = 500f
                                )
                            )
                        }
                    }
                )
            }
    ) {

        Box(
            Modifier
                .align(Alignment.Center)
                .offset {
                    IntOffset(
                        animatedTouch.value.x.toInt(),
                        animatedTouch.value.y.toInt()
                    )
                }
                .scale(3f)
                .alpha(buttonAlpha)
                .size(44.dp)
                .background(
                    Brush.radialGradient(
                        listOf(
                            AppTheme.colors.fg.copy(0.3f),
                            AppTheme.colors.fg.copy(0f)
                        )
                    )
                )
        )

        Text(
            title,
            style = AppTheme.typography.headline,
            fontWeight = FontWeight.SemiBold,
            color = AppTheme.colors.fg,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 18.dp)
        )
    }
}