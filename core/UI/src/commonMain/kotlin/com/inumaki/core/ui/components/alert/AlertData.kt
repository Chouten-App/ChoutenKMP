package com.inumaki.core.ui.components.alert

import androidx.compose.runtime.Composable

data class AlertData(
    val title: String,
    val content: @Composable () -> Unit
)