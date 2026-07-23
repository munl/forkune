package com.jian.forkune.utilities.preferences

/**
 * The app's single preferences wrapper. One typed property per stored value; the key
 * string is private, the property is a public `var` (clean-architecture §2b). Nullable
 * values are sentinel-encoded (empty string / -1) since the store holds primitives only.
 */
class AppPreferences(private val prefs: AppPreferencesInterface) {

    private val selectedLocationKey = "selectedLocationKey"
    var selectedLocation: String
        get() = prefs.getString(selectedLocationKey, default = DEFAULT_LOCATION)
        set(value) = prefs.setString(selectedLocationKey, value)

    private val lastPickNameKey = "lastPickNameKey"
    var lastPickName: String?
        get() = prefs.getStringOrNull(lastPickNameKey)?.takeIf { it.isNotEmpty() }
        set(value) = prefs.setString(lastPickNameKey, value ?: "")

    private val lastPickAtKey = "lastPickAtKey"
    var lastPickAt: Long?
        get() = prefs.getLong(lastPickAtKey, default = NULL_SENTINEL).takeIf { it != NULL_SENTINEL }
        set(value) = prefs.setLong(lastPickAtKey, value ?: NULL_SENTINEL)

    companion object {
        const val DEFAULT_LOCATION = "Mission District"
        private const val NULL_SENTINEL = -1L
    }
}
