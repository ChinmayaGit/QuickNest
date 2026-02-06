package com.chinmaya.myflowidgets

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.SharedPreferences
import android.widget.RemoteViews
import es.antonborri.home_widget.HomeWidgetProvider

class BatteryWidgetProvider : HomeWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
        widgetData: SharedPreferences,
    ) {
        val battery = widgetData.getInt("battery", -1)
        val percent = if (battery >= 0) battery else 0
        for (id in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_battery)
            views.setTextViewText(R.id.widget_value, if (battery >= 0) "$battery%" else "--%")
            views.setProgressBar(R.id.widget_progress, 100, percent, false)
            appWidgetManager.updateAppWidget(id, views)
        }
    }
}
