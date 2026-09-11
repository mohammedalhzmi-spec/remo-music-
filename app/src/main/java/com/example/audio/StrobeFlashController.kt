package com.example.audio

import android.content.Context
import android.hardware.camera2.CameraManager
import android.os.Build
import android.util.Log

class StrobeFlashController(private val context: Context) {
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private var cameraId: String? = null
    private var isFlashOn = false

    init {
        try {
            cameraId = cameraManager?.cameraIdList?.firstOrNull()
        } catch (e: Exception) {
            Log.w("StrobeFlash", "Failed to get camera id", e)
        }
    }

    fun pulseFlash() {
        if (cameraId == null || cameraManager == null) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                isFlashOn = !isFlashOn
                cameraManager.setTorchMode(cameraId!!, isFlashOn)
            }
        } catch (e: Exception) {
            // Camera might be busy or unavailable
        }
    }

    fun turnOff() {
        if (cameraId == null || cameraManager == null) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isFlashOn) {
                cameraManager.setTorchMode(cameraId!!, false)
                isFlashOn = false
            }
        } catch (e: Exception) {
            // Ignore
        }
    }
}
