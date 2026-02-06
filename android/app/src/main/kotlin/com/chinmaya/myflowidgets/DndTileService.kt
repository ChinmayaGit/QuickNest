package com.chinmaya.myflowidgets

import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.service.quicksettings.TileService

class DndTileService : TileService() {
    override fun onClick() {
        super.onClick()
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
        } else {
            Intent(Settings.ACTION_SOUND_SETTINGS)
        }.apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        startActivityAndCollapse(intent)
    }
}
