package com.inumaki.core.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.inumaki.core.ui.model.PosterData
import com.inumaki.core.ui.modifiers.shiningBorder
import com.inumaki.core.ui.theme.AppLayout
import com.inumaki.core.ui.theme.AppTheme

@Composable
fun PosterCard(data: PosterData, angle: Float) {

    val alphaVal = remember { Animatable(0.0f) }
    val scale = remember { Animatable(0.0f) }

    // Launch the scale animation when this item enters composition
    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
    }

    Column(
        verticalArrangement = Arrangement.spacedBy((-10).dp),
        modifier = Modifier
            .graphicsLayer { // apply the scale
                scaleX = scale.value
                scaleY = scale.value
                alpha = scale.value
            }
    ) {
        Box(
            modifier = Modifier
                .padding(bottom = 8.dp)
                .size(AppTheme.layout.posterSize)
                .shiningBorder(angle, 12.dp)
        ) {
            AsyncImage(
                model = data.poster,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(AppTheme.layout.posterSize)
            )
            Text(
                "~/~",
                style = AppTheme.typography.caption2,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .shiningBorder(angle, 12.dp)
                    .clip(RoundedCornerShape(50))
                    .background(AppTheme.colors.container)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }

        Text(
            data.title.primary,
            style = AppTheme.typography.caption1,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(start = 12.dp, top = 6.dp)
                .width(76.dp)
        )
    }
}