package com.inumaki.core.ui.modifiers

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.inumaki.core.ui.theme.AppTheme

@Composable
fun Modifier.shiningBorder(
    angle: Float,
    radius: Dp,
    backgroundColor: Color? = null
): Modifier {
    val colors = AppTheme.colors
    val background = backgroundColor ?: colors.container
    val sweepBrush = remember(colors) {
        Brush.sweepGradient(
            colors = listOf(
                colors.border,
                background,
                background,
                background,
                colors.border,
                colors.border,
                background,
                background,
                background,
                background,
                background,
                colors.border
            )
        )
    }

    return this
        .clip(RoundedCornerShape(radius))
        .drawWithContent {
            rotate(angle) {
                drawCircle(
                    brush = sweepBrush,
                    radius = size.width
                )
            }
            drawContent()
        }
        .padding(0.5.dp)
}