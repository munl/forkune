package com.jian.forkune.utilities.preferences

/**
 * Platform key-value store backing [AppPreferences]. Actuals: Android
 * SharedPreferences, iOS NSUserDefaults. Stores primitives only (clean-architecture §2b).
 */
interface AppPreferencesInterface {
    fun getString(key: String, default: String): String
    fun setString(key: String, value: String)

    fun getStringOrNull(key: String): String?

    fun getLong(key: String, default: Long): Long
    fun setLong(key: String, value: Long)

    fun getBool(key: String, default: Boolean): Boolean
    fun setBool(key: String, value: Boolean)
}
