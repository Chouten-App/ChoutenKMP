package com.inumaki.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.inumaki.core.ui.model.HomeRoute
import com.inumaki.core.ui.modifiers.shiningBorder


@Composable
fun AppImageButton(imageUrl: String, angle: Float, width: Dp, radius: Dp, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(width)
            .aspectRatio(1f)
            .shiningBorder(angle, radius)
            .clip(RoundedCornerShape(50))
            .background(Color(23, 23, 26))
            .padding(8.dp)
            .clickable(
                indication = null,
                interactionSource = null
            ) {
                onClick()
                // showProfile = true
            }
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .align(Alignment.Center)
                .clip(CircleShape),
        )
    }
}