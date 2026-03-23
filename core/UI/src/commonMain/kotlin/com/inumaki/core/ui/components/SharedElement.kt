package com.inumaki.core.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import com.inumaki.core.ui.theme.AppTheme

@Composable
fun SharedElement(
    key: String,
    isSource: Boolean = false, // true if this is the button / origin
    offset: Offset? = null,
    content: @Composable () -> Unit
) {
    val controller = AppTheme.controller
    val animating = controller.animatingKeys.contains(key)

    val movable = remember { movableContentOf(content) }

    // Capture bounds
    Box(
        Modifier
            .onGloballyPositioned { coords ->
                val bounds = coords.boundsInWindow().translate(offset ?: Offset.Zero)
                if (isSource) {
                    // Only register start bounds if this is the source
                    controller.registerStart(key, bounds, movable)
                } else {
                    // Only register end bounds if this is the destination
                    controller.registerEnd(key, bounds, movable)
                }
            }
            .alpha(if (animating) 0f else 1f)
    ) {
        // Hide the composable while animating
        content()
    }
}
