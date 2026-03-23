package com.inumaki.chouten

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

actual class GyroProvider: HeadingSource {

    private val _heading = MutableStateFlow(0f)
    override val heading: StateFlow<Float> = _heading

    override fun start() {
        _heading.value = 0f
    }

    override fun stop() {
    }
}