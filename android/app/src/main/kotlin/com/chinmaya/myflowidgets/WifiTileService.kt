package com.chinmaya.myflowidgets

import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.service.quicksettings.TileService

class WifiTileService : TileService() {
    override fun onClick() {
        super.onClick()
        @Suppress("DEPRECATION")
        val intent = Intent(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) Settings.ACTION_WIRELESS_SETTINGS else Settings.ACTION_WIFI_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivityAndCollapse(intent)
    }
}
