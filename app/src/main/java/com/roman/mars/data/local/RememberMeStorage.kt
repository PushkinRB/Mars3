package com.roman.mars.data.local

import android.content.Context

class RememberMeStorage(context: Context) {

    private val prefs = context.getSharedPreferences("mars_prefs", Context.MODE_PRIVATE)
    fun isRememberMeEnabled(): Boolean {
        return prefs.getBoolean(KEY_REMEMBER_ME, true)
    }
    fun setRememberMeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_REMEMBER_ME, enabled).apply()
    }
    companion object {
        private const val KEY_REMEMBER_ME = "remember_me"
    }
}