package com.chinmaya.myflowidgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.provider.Settings
import android.widget.RemoteViews
import es.antonborri.home_widget.HomeWidgetProvider

class NetSpeedWidgetProvider : HomeWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
        widgetData: SharedPreferences,
    ) {
        val speedText = widgetData.getString("netspeed_subtitle", null) ?: "Tap for usage"
        for (id in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_netspeed)
            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Intent(Settings.ACTION_DATA_USAGE_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
            } else {
                @Suppress("DEPRECATION")
                Intent(Settings.ACTION_WIRELESS_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
            }
            views.setOnClickPendingIntent(
                R.id.widget_root,
                PendingIntent.getActivity(context, 11, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE),
            )
            views.setTextViewText(R.id.widget_value, speedText)
            appWidgetManager.updateAppWidget(id, views)
        }
    }
}
