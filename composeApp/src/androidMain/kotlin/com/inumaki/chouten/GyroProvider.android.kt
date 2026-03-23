package com.inumaki.chouten

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.lang.Math.toDegrees

actual class GyroProvider(
    private val context: Context
): HeadingSource, SensorEventListener {

    private val _heading = MutableStateFlow(0f)
    override val heading: StateFlow<Float> = _heading

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    override fun start() {
        sensorManager.registerListener(
            this,
            rotationSensor,
            SensorManager.SENSOR_DELAY_UI
        )
    }

    override fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val rotationMatrix = FloatArray(9)
        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)

        val orientation = FloatArray(3)
        SensorManager.getOrientation(rotationMatrix, orientation)

        val azimuthRad = orientation[0]
        val azimuthDeg = toDegrees(azimuthRad.toDouble()).toFloat()

        _heading.value = normalizeDegrees(azimuthDeg)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}