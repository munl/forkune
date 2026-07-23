package com.jian.forkune.utilities.preferences

import android.content.Context

/**
 * Android [AppPreferencesInterface] backed by SharedPreferences. The Context is supplied
 * at Koin start (see initKoin(context)).
 */
class AndroidPreferences(context: Context) : AppPreferencesInterface {

    private val prefs = context.getSharedPreferences("forkune_prefs", Context.MODE_PRIVATE)

    override fun getString(key: String, default: String): String =
        prefs.getString(key, default) ?: default

    override fun setString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    override fun getStringOrNull(key: String): String? = prefs.getString(key, null)

    override fun getLong(key: String, default: Long): Long = prefs.getLong(key, default)

    override fun setLong(key: String, value: Long) {
        prefs.edit().putLong(key, value).apply()
    }

    override fun getBool(key: String, default: Boolean): Boolean = prefs.getBoolean(key, default)

    override fun setBool(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }
}
