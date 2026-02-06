package com.chinmaya.myflowidgets

import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.service.quicksettings.TileService

class DataTileService : TileService() {
    override fun onClick() {
        super.onClick()
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Intent(Settings.ACTION_DATA_USAGE_SETTINGS)
        } else {
            @Suppress("DEPRECATION")
            Intent(Settings.ACTION_WIRELESS_SETTINGS)
        }.apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        startActivityAndCollapse(intent)
    }
}
