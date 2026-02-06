package com.chinmaya.myflowidgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.widget.RemoteViews
import es.antonborri.home_widget.HomeWidgetProvider

class FlashWidgetProvider : HomeWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
        widgetData: SharedPreferences,
    ) {
        val on = widgetData.getBoolean("flash", false)
        for (id in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_flash)
            val intent = Intent(context, FlashToggleReceiver::class.java).apply {
                action = FlashToggleReceiver.ACTION_TOGGLE_FLASH
            }
            views.setOnClickPendingIntent(
                R.id.widget_root,
                PendingIntent.getBroadcast(context, 3, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE),
            )
            views.setTextViewText(R.id.widget_value, if (on) "On" else "Off")
            appWidgetManager.updateAppWidget(id, views)
        }
    }
}
