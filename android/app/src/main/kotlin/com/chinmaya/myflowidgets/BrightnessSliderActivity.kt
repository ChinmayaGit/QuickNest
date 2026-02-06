package com.chinmaya.myflowidgets

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.SeekBar
import android.widget.Toast

/**
 * Transparent overlay that shows only a brightness slider.
 * Opened when user taps the Brightness home screen widget. Does not open the full app.
 */
class BrightnessSliderActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_brightness_slider)

        val seekBar = findViewById<SeekBar>(R.id.seek_brightness)
        val textPercent = findViewById<android.widget.TextView>(R.id.text_percent)

        // Read initial value from HomeWidget prefs (same as Flutter uses)
        val prefs = getSharedPreferences("HomeWidgetPreferences", MODE_PRIVATE)
        val brightnessDouble = WidgetHelper.getDouble(prefs, "brightness", 0.8)
        val percent = (brightnessDouble * 100).toInt().coerceIn(0, 100)
        seekBar.progress = percent
        textPercent.text = "$percent%"

        // Tap outside (dark area) to close; inner card consumes taps
        findViewById<android.view.View>(R.id.root_brightness).setOnClickListener { finish() }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (!fromUser) return
                textPercent.text = "$progress%"
                val value = progress / 100.0
                setSystemBrightness(value)
                saveBrightnessAndUpdateWidget(value)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setSystemBrightness(value: Double) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(this)) {
            Toast.makeText(this, "Allow settings permission to change brightness", Toast.LENGTH_SHORT).show()
            startActivity(Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                data = android.net.Uri.parse("package:$packageName")
            })
            return
        }
        try {
            val brightness = (value * 255).toInt().coerceIn(0, 255)
            Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, brightness)
            // Also set window brightness so it applies immediately
            window.attributes = window.attributes.apply { screenBrightness = value.toFloat() }
        } catch (e: SecurityException) {
            // Fallback: only set this window's brightness
            window.attributes = window.attributes.apply { screenBrightness = value.toFloat() }
        }
    }

    private fun saveBrightnessAndUpdateWidget(value: Double) {
        val prefs = getSharedPreferences("HomeWidgetPreferences", MODE_PRIVATE).edit()
        prefs.putBoolean("home_widget.double.brightness", true)
        prefs.putLong("brightness", java.lang.Double.doubleToRawLongBits(value))
        prefs.apply()
        // Refresh the brightness widget
        val intent = Intent(this, BrightnessWidgetProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
                AppWidgetManager.getInstance(this@BrightnessSliderActivity)
                    .getAppWidgetIds(ComponentName(this@BrightnessSliderActivity, BrightnessWidgetProvider::class.java)))
        }
        sendBroadcast(intent)
    }
}
