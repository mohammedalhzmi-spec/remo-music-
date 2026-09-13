package com.example.audio

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

class ShakeGestureController(
    context: Context,
    private val onShakeLeft: () -> Unit,   // Next song
    private val onShakeRight: () -> Unit,  // Previous song
    private val onShakeDown: () -> Unit,   // Pause
    private val onShakeUp: () -> Unit      // Play
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private var isListening = false

    private var lastUpdate = 0L
    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f

    fun startListening() {
        if (!isListening && sensorManager != null && accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
            isListening = true
        }
    }

    fun stopListening() {
        if (isListening && sensorManager != null) {
            sensorManager.unregisterListener(this)
            isListening = false
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val curTime = System.currentTimeMillis()
            if ((curTime - lastUpdate) > 400) {
                val diffTime = (curTime - lastUpdate)
                lastUpdate = curTime

                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                val speed = sqrt(((x - lastX) * (x - lastX) + (y - lastY) * (y - lastY) + (z - lastZ) * (z - lastZ)).toDouble()) / diffTime * 10000

                if (speed > 750) { // Shake detected
                    // Determine direction based on dominant axis change
                    when {
                        x > 6f -> onShakeRight()  // Tilt/Shake right -> Previous
                        x < -6f -> onShakeLeft() // Tilt/Shake left -> Next
                        y > 6f -> onShakeDown()  // Shake down -> Pause
                        y < -6f -> onShakeUp()   // Shake up -> Play
                        else -> onShakeLeft()    // Default to Next
                    }
                }

                lastX = x
                lastY = y
                lastZ = z
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
