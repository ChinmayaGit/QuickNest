package com.chinmaya.myflowidgets

import android.content.Intent
import android.service.quicksettings.TileService

class FullscreenTileService : TileService() {
    override fun onClick() {
        super.onClick()
        startActivityAndCollapse(Intent(this, FullscreenActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }
}
