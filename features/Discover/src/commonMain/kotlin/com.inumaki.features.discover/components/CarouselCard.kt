package com.inumaki.features.discover.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode.Companion.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.inumaki.core.ui.components.AppButton
import com.inumaki.core.ui.components.AppTintedButton
import com.inumaki.core.ui.components.ConcentricShape
import com.inumaki.core.ui.modifiers.shiningBorder
import com.inumaki.core.ui.model.PosterData
import com.inumaki.core.ui.theme.AppTheme

@Composable
fun CarouselCard(data: PosterData, angle: Float) {
    Box(
        modifier = Modifier
            .padding(24.dp)
            .widthIn(max = 460.dp)
            .fillMaxWidth()
            //.shiningBorder(angle, 34.dp)
            .clip(ConcentricShape(34.dp))
            .background(AppTheme.colors.container),
        contentAlignment = Alignment.TopEnd
    ) {
        AsyncImage(
            model = data.poster,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.65f)
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0x00FFFFFF and AppTheme.colors.container.toArgb()),
                            AppTheme.colors.container
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column {
                Text(
                    data.title.secondary ?: "",
                    style = AppTheme.typography.caption2,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    modifier = Modifier.alpha(0.7f)
                )

                Text(
                    data.title.primary,
                    style = AppTheme.typography.title2,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                )
            }

            Text(
                data.description ?: "",
                style = AppTheme.typography.caption1,
                maxLines = 3,
                fontWeight = FontWeight.Medium,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.alpha(0.7f)
            )
            Row {
                AppButton("drawable/plus-solid-full.svg", angle, modifier = Modifier.padding(end = 12.dp))

                AppTintedButton("Start Episode 1", modifier = Modifier.fillMaxWidth())
            }
        }
    }
}