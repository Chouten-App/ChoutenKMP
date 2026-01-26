package com.inumaki.core.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import chouten.core.ui.generated.resources.Res
import coil3.compose.AsyncImage
import com.inumaki.core.ui.theme.AppTheme

@Composable
fun AppBottomBarItem(
    title: String,
    icon: String,
    isSelected: Boolean,
    showLabel: Boolean,
    modifier: Modifier = Modifier
) {
    val transition = updateTransition(
        targetState = showLabel,
        label = "LabelTransition"
    )

    val labelAlpha by transition.animateFloat(
        transitionSpec = {
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        },
        label = "LabelAlpha"
    ) { visible ->
        if (visible) 1f else 0f
    }

    val labelScale by transition.animateFloat(
        transitionSpec = {
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        },
        label = "LabelScale"
    ) { visible ->
        if (visible) 1f else 0.8f
    }

    val iconScale by transition.animateFloat(
        transitionSpec = {
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        },
        label = "IconScale"
    ) { visible ->
        if (visible) 0.725f else 1.2f
    }

    val iconOffsetY by transition.animateDp(
        transitionSpec = {
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        },
        label = "IconOffsetY"
    ) { visible ->
        if (visible) 2.dp else 10.dp
    }

    Column(
        modifier = modifier
            .padding(top = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        AsyncImage(
            Res.getUri(icon),
            contentDescription = title,
            colorFilter = ColorFilter.tint(AppTheme.colors.fg, BlendMode.SrcIn),
            modifier = Modifier
                .offset(y = if (AppTheme.layout.forceHideLabels) 8.dp else iconOffsetY)
                .width(24.dp)
                .aspectRatio(1f)
                .scale(if (AppTheme.layout.forceHideLabels) 0.9f else iconScale)
                .alpha(if (isSelected) 1.0f else 0.7f)
        )

        if (!AppTheme.layout.forceHideLabels) {
            Text(
                title,
                style = AppTheme.typography.caption2,
                modifier = Modifier
                    .alpha(labelAlpha * if (isSelected) 1f else 0.7f)
                    .scale(labelScale)
            )
        }
    }
}