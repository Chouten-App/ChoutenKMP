package com.inumaki.core.ui.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object ScrollGate {
    var canDrag by mutableStateOf(true)
        private set

    fun update(atTop: Boolean, isEmpty: Boolean) {
        canDrag = isEmpty || atTop
    }
}