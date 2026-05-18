package com.inumaki.core.ui.components

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import chouten.core.ui.generated.resources.Res
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import com.inumaki.core.ui.theme.AppTheme
import dev.chouten.core.repository.FileStore
import io.ktor.http.ContentDisposition.Companion.File

@Composable
fun AppImage(icon: String, title: String? = null, modifier: Modifier = Modifier, color: Color = AppTheme.colors.fg) {
    AsyncImage(
        Res.getUri(icon),
        contentDescription = title,
        colorFilter = ColorFilter.tint(color, BlendMode.SrcIn),
        modifier = modifier
    )
}

@Composable
fun AppAsyncImage(icon: String, title: String? = null, modifier: Modifier = Modifier) {
    val data = if (icon.startsWith("http://") || icon.startsWith("https://")) {
        icon
    } else {
        "file://${FileStore.documentsDir()}$icon"
    }

    println(data)

    AsyncImage(
        model = data,
        contentDescription = title,
        modifier = modifier
    )
}
