package com.chinmaya.myflowidgets

import android.content.Intent
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

class BrightnessTileService : TileService() {
    override fun onClick() {
        super.onClick()
        startActivityAndCollapse(Intent(this, BrightnessSliderActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }
}
