package com.jian.forkune.utilities.preferences

import platform.Foundation.NSUserDefaults

/**
 * iOS [AppPreferencesInterface] backed by NSUserDefaults.standardUserDefaults.
 */
class IosPreferences : AppPreferencesInterface {

    private val defaults = NSUserDefaults.standardUserDefaults

    override fun setString(key: String, value: String) {
        defaults.setObject(value, forKey = key)
    }

    override fun getStringOrNull(key: String): String? = defaults.stringForKey(key)

    override fun setLong(key: String, value: Long) {
        defaults.setInteger(value, forKey = key)
    }

    override fun getLong(key: String, default: Long): Long =
        if (defaults.objectForKey(key) == null) default else defaults.integerForKey(key)
}
