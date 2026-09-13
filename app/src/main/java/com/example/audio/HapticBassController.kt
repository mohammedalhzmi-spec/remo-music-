package com.example.audio

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

class HapticBassController(private val context: Context) {

    private val vibrator: Vibrator? by lazy {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                manager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (e: Exception) {
            Log.w("HapticBass", "Failed to access vibrator service", e)
            null
        }
    }

    private var lastPulseTime = 0L

    fun triggerBassHaptic(bassIntensity: Float, threshold: Float = 0.72f) {
        if (vibrator == null || !vibrator!!.hasVibrator()) return
        val now = System.currentTimeMillis()
        // Minimum interval between haptic pulses to prevent continuous buzz
        if (now - lastPulseTime < 130L) return

        if (bassIntensity >= threshold) {
            lastPulseTime = now
            val strength = ((bassIntensity - threshold) / (1f - threshold)).coerceIn(0.1f, 1.0f)
            val amplitude = (strength * 255).toInt().coerceIn(40, 255)

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val durationMs = (35 + strength * 45).toLong()
                    val effect = VibrationEffect.createOneShot(durationMs, amplitude)
                    vibrator?.vibrate(effect)
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(40)
                }
            } catch (e: Exception) {
                // Ignore vibration errors gracefully
            }
        }
    }

    fun stop() {
        try {
            vibrator?.cancel()
        } catch (e: Exception) {
            // Ignore
        }
    }
}
