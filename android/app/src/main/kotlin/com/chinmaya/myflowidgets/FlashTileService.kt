package com.chinmaya.myflowidgets

import android.content.ComponentName
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast

class FlashTileService : TileService() {
    override fun onClick() {
        super.onClick()
        toggleFlash()
    }

    private fun toggleFlash() {
        val cam = getSystemService(CAMERA_SERVICE) as? CameraManager ?: return
        val cameraId = try {
            cam.cameraIdList.find { id ->
                cam.getCameraCharacteristics(id).get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Flash not available", Toast.LENGTH_SHORT).show()
            return
        }
        if (cameraId == null) {
            Toast.makeText(this, "Flash not available", Toast.LENGTH_SHORT).show()
            return
        }
        val prefs = getSharedPreferences("HomeWidgetPreferences", MODE_PRIVATE)
        val newState = !prefs.getBoolean("flash", false)
        try {
            cam.setTorchMode(cameraId, newState)
            prefs.edit().putBoolean("flash", newState).apply()
            qsTile?.state = if (newState) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            qsTile?.updateTile()
            sendBroadcast(Intent(this, FlashWidgetProvider::class.java).apply {
                action = android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_IDS,
                    android.appwidget.AppWidgetManager.getInstance(this@FlashTileService)
                        .getAppWidgetIds(ComponentName(this@FlashTileService, FlashWidgetProvider::class.java)))
            })
        } catch (e: Exception) {
            Toast.makeText(this, "Could not toggle flash", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStartListening() {
        super.onStartListening()
        val prefs = getSharedPreferences("HomeWidgetPreferences", MODE_PRIVATE)
        qsTile?.state = if (prefs.getBoolean("flash", false)) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        qsTile?.updateTile()
    }
}
