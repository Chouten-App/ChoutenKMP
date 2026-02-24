package com.inumaki.core.ui.components

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import chouten.core.ui.generated.resources.Res
import coil3.compose.AsyncImage
import com.inumaki.core.ui.theme.AppTheme

@Composable
fun AppImage(icon: String, title: String? = null, modifier: Modifier = Modifier) {
    AsyncImage(
        Res.getUri(icon),
        contentDescription = title,
        colorFilter = ColorFilter.tint(AppTheme.colors.fg, BlendMode.SrcIn),
        modifier = modifier
    )
}