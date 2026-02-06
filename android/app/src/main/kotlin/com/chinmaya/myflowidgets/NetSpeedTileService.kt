package com.chinmaya.myflowidgets

import android.content.Intent
import android.net.TrafficStats
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

/**
 * Quick Settings tile that shows current network speed (↓/↑) and opens data usage on tap.
 */
class NetSpeedTileService : TileService() {

    private val handler = Handler(Looper.getMainLooper())
    private var lastRxBytes = 0L
    private var lastTxBytes = 0L
    private var lastTime = 0L

    private val updateRunnable = object : Runnable {
        override fun run() {
            updateSpeed()
            handler.postDelayed(this, 2000)
        }
    }

    override fun onStartListening() {
        super.onStartListening()
        lastRxBytes = TrafficStats.getTotalRxBytes()
        lastTxBytes = TrafficStats.getTotalTxBytes()
        lastTime = System.currentTimeMillis()
        qsTile?.label = getString(R.string.widget_netspeed)
        qsTile?.subtitle = "…"
        qsTile?.updateTile()
        handler.postDelayed(updateRunnable, 1500)
    }

    override fun onStopListening() {
        super.onStopListening()
        handler.removeCallbacks(updateRunnable)
    }

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

    private fun updateSpeed() {
        val qsTile = qsTile ?: return
        val now = System.currentTimeMillis()
        val rx = TrafficStats.getTotalRxBytes()
        val tx = TrafficStats.getTotalTxBytes()
        val elapsed = (now - lastTime) / 1000.0
        if (elapsed > 0) {
            val rxRate = (rx - lastRxBytes).toDouble() / elapsed
            val txRate = (tx - lastTxBytes).toDouble() / elapsed
            lastRxBytes = rx
            lastTxBytes = tx
            lastTime = now
            val down = formatSpeed(rxRate)
            val up = formatSpeed(txRate)
            qsTile.subtitle = "↓$down ↑$up"
        }
        qsTile.label = getString(R.string.widget_netspeed)
        qsTile.updateTile()
    }

    private fun formatSpeed(bytesPerSec: Double): String {
        return when {
            bytesPerSec >= 1_000_000 -> "%.1fMB/s".format(bytesPerSec / 1_000_000)
            bytesPerSec >= 1_000 -> "%.1fKB/s".format(bytesPerSec / 1_000)
            else -> "%.0fB/s".format(bytesPerSec)
        }
    }
}
