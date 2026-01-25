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

@Composable
fun PosterCard(data: PosterData, angle: Float) {

    val alphaVal = remember { Animatable(0.5f) }
    val scale = remember { Animatable(0.8f) }

    // Launch the scale animation when this item enters composition
    LaunchedEffect(Unit) {
        alphaVal.animateTo(
            targetValue = 1f,
        )
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
                alpha = alphaVal.value
            }
    ) {
        Box(
            modifier = Modifier
                .padding(bottom = 8.dp)
                .width(100.dp)
                .aspectRatio(0.7f)
                .shiningBorder(angle, 12.dp)
        ) {
            AsyncImage(
                model = data.poster,
                contentDescription = null,
                contentScale = ContentScale.FillHeight,
                modifier = Modifier
                    .width(100.dp)
                    .aspectRatio(0.7f)
            )
            Text(
                "̃~/~",
                fontSize = 10.sp,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .shiningBorder(angle, 12.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xff17171A))
                    .padding(horizontal = 8.dp, vertical = 0.dp)
            )
        }

        Text(
            data.title.primary,
            fontSize = 12.sp,
            maxLines = 2,
            lineHeight = 14.sp,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(start = 12.dp, top = 6.dp)
                .width(76.dp)
        )
    }
}