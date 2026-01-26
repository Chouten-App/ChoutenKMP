package com.inumaki.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inumaki.core.ui.model.TopBarConfig
import com.inumaki.core.ui.theme.AppTheme


@Composable
fun AppTopBar(topBarConfig: TopBarConfig?, angle: Float, modifier: Modifier = Modifier) {
    topBarConfig?.let { config ->
        val statusInsets = WindowInsets.statusBars.asPaddingValues()
        Row(
            modifier = modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        0.0f to Color(0xff0c0c0c),
                        1.0f to Color(0x000c0c0c),
                        start = Offset(0f, 0f),
                        end = Offset(0f, 100f)
                    )
                )
                .padding(
                    start = AppTheme.layout.screenEdgePadding.calculateLeftPadding(LayoutDirection.Ltr),
                    top = AppTheme.layout.screenEdgePadding.calculateTopPadding() + statusInsets.calculateTopPadding(),
                    end = AppTheme.layout.screenEdgePadding.calculateRightPadding(LayoutDirection.Ltr)
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(config.title, style = AppTheme.typography.title3, fontWeight = FontWeight.Bold)

            config.actions.forEach { action ->
                AppButton("drawable/${action.icon}", angle)
            }
        }
    }
}