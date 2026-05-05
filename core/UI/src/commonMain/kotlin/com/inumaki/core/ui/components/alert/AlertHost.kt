package com.inumaki.core.ui.components.alert

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inumaki.core.ui.modifiers.shiningBorder
import com.inumaki.core.ui.theme.AppTheme

@Composable
fun AlertHost(manager: AlertManager) {
    var visibleEntry by remember { mutableStateOf<AlertData?>(null) }
    val current = manager.current

    LaunchedEffect(current) {
        if (current != null) {
            visibleEntry = current
        }
    }

    val isVisible = current != null

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 0.5f else 0f,
        animationSpec = tween(200)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha))
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn() + scaleIn(initialScale = 1.3f),
            exit = fadeOut() + scaleOut(targetScale = 0.8f),
            modifier = Modifier.align(Alignment.Center)
        ) {
            val entry = visibleEntry ?: return@AnimatedVisibility
            Box(
                modifier = Modifier
                    .padding(52.dp)
                    .fillMaxWidth()
                    .shiningBorder(60f, 32.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(AppTheme.colors.background)
                    .padding(14.dp)
            ) {
                Column {
                    Text(
                        entry.title,
                        style = TextStyle(
                            fontWeight = FontWeight.SemiBold,
                            color = AppTheme.colors.fg,
                            fontSize = 18.sp
                        ),
                        modifier = Modifier
                            .padding(start = 10.dp, end = 10.dp, top = 10.dp)
                    )

                    entry.content()
                }
            }
        }
    }
}