package com.inumaki.chouten

import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import com.inumaki.core.ui.createDataStore

fun main() = application {
    System.setProperty("skiko.render.backend", "wayland")
    val headingProvider = remember { GyroProvider() }
    Window(
        onCloseRequest = ::exitApplication,
        state = WindowState(
            placement = WindowPlacement.Floating,
            position = WindowPosition.PlatformDefault,
            size = DpSize(366.dp, 895.dp),
            isMinimized = false
        ),
        title = "Chouten",
    ) {
        App(headingProvider, createDataStore())
    }
}