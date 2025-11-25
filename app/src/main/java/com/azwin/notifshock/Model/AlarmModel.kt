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

    fun setAppEnabled(appKey: String, isEnabled: Boolean) {
        prefs.edit().putBoolean(appKey, isEnabled).apply()
    }

    fun isAppEnabled(appKey: String): Boolean {
        // Default to true if not set
        return prefs.getBoolean(appKey, true)
    }

    fun saveLockState(isLocked: Boolean) {
        prefs.edit().putBoolean("KEY_IS_LOCKED", isLocked).apply()
    }

    fun getLockState(): Boolean {
        return prefs.getBoolean("KEY_IS_LOCKED", false)
    }
}
