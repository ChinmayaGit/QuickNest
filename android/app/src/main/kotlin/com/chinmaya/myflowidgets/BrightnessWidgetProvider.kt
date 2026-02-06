package com.chinmaya.myflowidgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.widget.RemoteViews
import es.antonborri.home_widget.HomeWidgetProvider

class BrightnessWidgetProvider : HomeWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
        widgetData: SharedPreferences,
    ) {
        val brightnessDouble = WidgetHelper.getDouble(widgetData, "brightness", -1.0)
        val percent = if (brightnessDouble >= 0) (brightnessDouble * 100).toInt() else 0
        for (id in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_brightness)
            setClickToOpenBrightnessSlider(context, views)
            views.setTextViewText(R.id.widget_value, if (brightnessDouble >= 0) "$percent%" else "--%")
            views.setProgressBar(R.id.widget_progress, 100, percent, false)
            appWidgetManager.updateAppWidget(id, views)
        }
    }

    private fun setClickToOpenBrightnessSlider(context: Context, views: RemoteViews) {
        val intent = Intent(context, BrightnessSliderActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY
        }
        views.setOnClickPendingIntent(
            R.id.widget_root,
            PendingIntent.getActivity(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE),
        )
    }
}
