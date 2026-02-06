package com.chinmaya.myflowidgets

import android.content.SharedPreferences

object WidgetHelper {
    private const val DOUBLE_PREFIX = "home_widget.double."

    fun getDouble(prefs: SharedPreferences, key: String, default: Double): Double {
        return if (prefs.getBoolean(DOUBLE_PREFIX + key, false)) {
            java.lang.Double.longBitsToDouble(prefs.getLong(key, 0))
        } else {
            default
        }
    }
}
