package com.chinmaya.myflowidgets

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.widget.SeekBar

/**
 * Overlay that shows only a volume slider. Opened when user taps the Volume widget.
 * Does not open the full app. Uses native AudioManager.
 */
class VolumeSliderActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_volume_slider)

        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        val streamType = AudioManager.STREAM_MUSIC
        val maxVolume = audioManager.getStreamMaxVolume(streamType)
        val currentVolume = audioManager.getStreamVolume(streamType)
        val percent = if (maxVolume > 0) (currentVolume * 100 / maxVolume) else 0

        val seekBar = findViewById<SeekBar>(R.id.seek_volume)
        val textPercent = findViewById<android.widget.TextView>(R.id.text_percent)

        seekBar.max = 100
        seekBar.progress = percent
        textPercent.text = "$percent%"

        findViewById<android.view.View>(R.id.root_volume).setOnClickListener { finish() }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (!fromUser) return
                textPercent.text = "$progress%"
                val volumeIndex = (progress * maxVolume / 100).coerceIn(0, maxVolume)
                audioManager.setStreamVolume(streamType, volumeIndex, 0)
                saveVolumeAndUpdateWidget(progress / 100.0)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun saveVolumeAndUpdateWidget(value: Double) {
        val prefs = getSharedPreferences("HomeWidgetPreferences", MODE_PRIVATE).edit()
        prefs.putBoolean("home_widget.double.volume", true)
        prefs.putLong("volume", java.lang.Double.doubleToRawLongBits(value))
        prefs.apply()
        val intent = Intent(this, VolumeWidgetProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
                AppWidgetManager.getInstance(this@VolumeSliderActivity)
                    .getAppWidgetIds(ComponentName(this@VolumeSliderActivity, VolumeWidgetProvider::class.java)))
        }
        sendBroadcast(intent)
    }
}
