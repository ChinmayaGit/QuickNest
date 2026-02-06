package com.chinmaya.myflowidgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.widget.RemoteViews
import es.antonborri.home_widget.HomeWidgetProvider

class VolumeWidgetProvider : HomeWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
        widgetData: SharedPreferences,
    ) {
        val volumeDouble = WidgetHelper.getDouble(widgetData, "volume", -1.0)
        val percent = if (volumeDouble >= 0) (volumeDouble * 100).toInt() else 0
        for (id in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_volume)
            val intent = Intent(context, VolumeSliderActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY
            }
            views.setOnClickPendingIntent(
                R.id.widget_root,
                PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE),
            )
            views.setTextViewText(R.id.widget_value, if (volumeDouble >= 0) "$percent%" else "--%")
            views.setProgressBar(R.id.widget_progress, 100, percent, false)
            appWidgetManager.updateAppWidget(id, views)
        }
    }
}
