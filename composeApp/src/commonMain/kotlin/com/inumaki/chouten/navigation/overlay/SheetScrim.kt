package com.inumaki.chouten.navigation.overlay

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color

/**
 * Dimmed background overlay for sheet presentations.
 *
 * Provides a semi-transparent black background that dims the content
 * behind a sheet and allows dismissal by clicking outside.
 *
 * @param onDismiss Callback when the scrim is clicked
 * @param visible Whether the scrim should be visible
 */
@Composable
fun SheetScrim(
    onDismiss: () -> Unit,
    visible: Boolean = true
) {
    val transition = updateTransition(
        targetState = visible,
        label = "SheetScrim"
    )

    val alpha by transition.animateFloat(
        label = "ScrimAlpha"
    ) { isVisible ->
        if (isVisible) 0.6f else 0f
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(alpha)
            .background(Color.Black)
            .clickable(onClick = onDismiss)
    )
}