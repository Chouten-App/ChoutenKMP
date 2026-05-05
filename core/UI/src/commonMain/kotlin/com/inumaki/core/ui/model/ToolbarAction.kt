package com.inumaki.core.ui.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import com.inumaki.core.ui.theme.LocalToolbarItems

data class ToolbarAction(
    val icon: String,
    val onClick: () -> Unit
)

fun Modifier.toolbar(
    content: @Composable () -> Unit
): Modifier = composed {
    val items = LocalToolbarItems.current

    DisposableEffect(Unit) {
        items.add(content)
        onDispose { items.remove(content) }
    }

    this
}