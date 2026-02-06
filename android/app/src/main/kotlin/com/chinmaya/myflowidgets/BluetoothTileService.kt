package com.chinmaya.myflowidgets

import android.content.Intent
import android.provider.Settings
import android.service.quicksettings.TileService

class BluetoothTileService : TileService() {
    override fun onClick() {
        super.onClick()
        @Suppress("DEPRECATION")
        startActivityAndCollapse(Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }
}
