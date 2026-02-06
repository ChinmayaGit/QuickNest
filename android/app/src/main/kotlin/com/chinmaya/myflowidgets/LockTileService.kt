package com.chinmaya.myflowidgets

import android.content.Intent
import android.provider.Settings
import android.service.quicksettings.TileService

class LockTileService : TileService() {
    override fun onClick() {
        super.onClick()
        startActivityAndCollapse(Intent(Settings.ACTION_SECURITY_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }
}
