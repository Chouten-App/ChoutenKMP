package com.inumaki.chouten

import androidx.compose.ui.window.ComposeUIViewController
import androidx.compose.runtime.remember

fun MainViewController() = ComposeUIViewController {
    val headingProvider = remember { GyroProvider() }
    App(headingProvider)
}