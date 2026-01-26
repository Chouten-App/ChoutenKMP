package com.inumaki.chouten

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import platform.CoreMotion.CMMotionManager
import platform.Foundation.NSOperationQueue
import kotlin.math.PI

actual class GyroProvider: HeadingSource {

    private val motionManager = CMMotionManager()
    private val _heading = MutableStateFlow(0f)
    override val heading: StateFlow<Float> = _heading

    override fun start() {
        if (!motionManager.deviceMotionAvailable) return

        motionManager.deviceMotionUpdateInterval = 1.0 / 60.0
        motionManager.startDeviceMotionUpdatesToQueue(
            NSOperationQueue.mainQueue()
        ) { motion, _ ->
            motion ?: return@startDeviceMotionUpdatesToQueue

            val yawRad = motion.attitude.yaw
            val yawDeg = (yawRad * 180.0 / PI).toFloat()

            _heading.value = normalizeDegrees(yawDeg)
        }
    }

    override fun stop() {
        motionManager.stopDeviceMotionUpdates()
    }
}
