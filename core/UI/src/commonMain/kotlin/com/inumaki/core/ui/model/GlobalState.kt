package com.inumaki.core.ui.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object GlobalState {
    // MutableStateFlow so it can be observed in Compose
    private val _angle = MutableStateFlow(0f)
    val angle: StateFlow<Float> = _angle

    fun setAngle(value: Float) {
        _angle.value = value
    }
}