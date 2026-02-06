package com.chinmaya.myflowidgets

import android.content.Intent
import android.service.quicksettings.TileService
import android.provider.Settings

class BatteryTileService : TileService() {
    override fun onClick() {
        super.onClick()
        startActivityAndCollapse(Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }
}
