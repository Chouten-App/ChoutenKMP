package com.inumaki.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import chouten.core.ui.generated.resources.Res
import coil3.compose.AsyncImage
import com.inumaki.core.ui.modifiers.shiningBorder
import com.inumaki.core.ui.theme.AppTheme

@Composable
fun AppButton(iconPath: String, angle: Float, modifier: Modifier = Modifier, background: Color = AppTheme.colors.container, onClick: () -> Unit = {}) {
    Box(
        modifier = modifier
            .size(AppTheme.layout.iconSize)
            .shiningBorder(angle, AppTheme.layout.iconSize.width / 2, background)
            .clip(RoundedCornerShape(50))
            .background(background)
            .clickable {
                onClick()
            },
    ) {
        AsyncImage(
            Res.getUri(iconPath),
            contentDescription = "",
            colorFilter = ColorFilter.tint(AppTheme.colors.fg, BlendMode.SrcIn),
            modifier = Modifier
                .align(Alignment.Center)
                .width(AppTheme.layout.iconSize.width)
                .aspectRatio(1f)
                .scale(0.45f)
                .alpha(0.7f)
        )
    }
}