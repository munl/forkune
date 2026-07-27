package com.jian.forkune.utilities.preferences

/**
 * Platform key-value store backing [AppPreferences]. Actuals: Android
 * SharedPreferences, iOS NSUserDefaults. Stores primitives only (clean-architecture §2b).
 */
interface AppPreferencesInterface {
    fun setString(key: String, value: String)
    fun getStringOrNull(key: String): String?

    fun setLong(key: String, value: Long)
    fun getLong(key: String, default: Long): Long
}
