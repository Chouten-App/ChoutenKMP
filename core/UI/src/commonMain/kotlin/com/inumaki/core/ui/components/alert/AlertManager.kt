package com.inumaki.core.ui.components.alert

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class AlertManager {

    // null = no alert
    var current: AlertData? by mutableStateOf(null)
        private set

    fun present(entry: AlertData) {
        current = entry
    }

    fun dismiss(title: String = "") {
        val active = current ?: return
        if (active.title == title) {
            current = null
        }
    }
}