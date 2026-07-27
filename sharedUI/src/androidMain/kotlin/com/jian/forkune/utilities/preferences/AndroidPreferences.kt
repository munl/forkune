package com.jian.forkune.utilities.preferences

import android.content.Context
import androidx.core.content.edit

/**
 * Android [AppPreferencesInterface] backed by SharedPreferences. The Context is supplied
 * at Koin start (see initKoin(context)).
 */
class AndroidPreferences(context: Context) : AppPreferencesInterface {

    private val prefs = context.getSharedPreferences("forkune_prefs", Context.MODE_PRIVATE)

    override fun setString(key: String, value: String) = prefs.edit { putString(key, value) }

    override fun getStringOrNull(key: String): String? = prefs.getString(key, null)

    override fun setLong(key: String, value: Long) = prefs.edit { putLong(key, value) }

    override fun getLong(key: String, default: Long): Long = prefs.getLong(key, default)
}
