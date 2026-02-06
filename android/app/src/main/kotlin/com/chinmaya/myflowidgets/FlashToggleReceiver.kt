package com.chinmaya.myflowidgets

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.os.Build
import android.widget.Toast

/**
 * Toggles flashlight when user taps the Flash widget. Does not open the app.
 */
class FlashToggleReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_TOGGLE_FLASH) return
        val cam = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager ?: return
        var cameraId: String? = null
        try {
            cameraId = cam.cameraIdList.find { id ->
                cam.getCameraCharacteristics(id).get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Flash not available", Toast.LENGTH_SHORT).show()
            return
        }
        if (cameraId == null) {
            Toast.makeText(context, "Flash not available", Toast.LENGTH_SHORT).show()
            return
        }
        val prefs = context.getSharedPreferences("HomeWidgetPreferences", Context.MODE_PRIVATE)
        val currentlyOn = prefs.getBoolean("flash", false)
        val newState = !currentlyOn
        try {
            cam.setTorchMode(cameraId, newState)
            prefs.edit().putBoolean("flash", newState).apply()
            updateWidget(context)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not toggle flash", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateWidget(context: Context) {
        val intent = Intent(context, FlashWidgetProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
                AppWidgetManager.getInstance(context).getAppWidgetIds(ComponentName(context, FlashWidgetProvider::class.java)))
        }
        context.sendBroadcast(intent)
    }

    companion object {
        const val ACTION_TOGGLE_FLASH = "com.chinmaya.myflowidgets.TOGGLE_FLASH"
    }
}
