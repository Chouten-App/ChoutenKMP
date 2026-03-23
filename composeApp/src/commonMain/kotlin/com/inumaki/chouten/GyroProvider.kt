package com.inumaki.chouten

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

fun normalizeDegrees(deg: Float): Float {
    return ((deg % 360f) + 360f) % 360f
}

interface HeadingSource {
    val heading: StateFlow<Float>
    fun start() {}
    fun stop() {}
}

expect class GyroProvider: HeadingSource

class PreviewHeadingSource(
    initialAngle: Float = 45f
) : HeadingSource {

    private val _heading = MutableStateFlow(initialAngle)
    override val heading: StateFlow<Float> = _heading
}