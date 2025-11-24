package com.azwin.notifshock.Model

import android.content.Context

class AlarmModel(context: Context) {
    private val prefs = context.getSharedPreferences("TeleSirenPrefs", Context.MODE_PRIVATE)

    fun saveTargetKeyword(keyword: String) {
        prefs.edit().putString("KEY_TARGET", keyword).apply()
    }

    fun getTargetKeyword(): String {
        return prefs.getString("KEY_TARGET", "") ?: ""
    }
}
