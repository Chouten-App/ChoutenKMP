package com.inumaki.core.ui.components

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

class ConcentricShape(
    private val cornerRadius: Dp,
    private val inset: Dp = 0.dp
) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline = with(density) {

        val radiusPx = cornerRadius.toPx()
        val insetPx = inset.toPx()

        val adjustedRadius = (radiusPx - insetPx)
            .coerceAtLeast(0f)
            .coerceAtMost((size.minDimension - insetPx * 2f) / 2f)

        val rect = Rect(
            insetPx,
            insetPx,
            size.width - insetPx,
            size.height - insetPx
        )

        Outline.Rounded(
            RoundRect(
                rect = rect,
                cornerRadius = CornerRadius(adjustedRadius, adjustedRadius)
            )
        )
    }
}