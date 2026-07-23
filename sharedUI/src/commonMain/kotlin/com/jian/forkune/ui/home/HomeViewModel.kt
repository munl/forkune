package com.jian.forkune.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jian.forkune.data.places.PlacesRepository
import com.jian.forkune.utilities.preferences.AppPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/** The Home footer's "Last pick: {name} · {daysAgo} days ago" data. */
data class LastPick(
    val name: String,
    val daysAgo: Long,
)

/**
 * Home screen state (clean-architecture §3). Exposes the location label, the nearby-place
 * count and the last-pick footer as [kotlinx.coroutines.flow.StateFlow]; reads them from
 * [AppPreferences] and [PlacesRepository]. No Compose / platform imports.
 */
class HomeViewModel(
    private val preferences: AppPreferences,
    private val placesRepository: PlacesRepository,
) : ViewModel() {

    private val locationInternal = MutableStateFlow(AppPreferences.DEFAULT_LOCATION)
    val location = locationInternal.asStateFlow()

    private val placeCountInternal = MutableStateFlow(0)
    val placeCount = placeCountInternal.asStateFlow()

    private val lastPickInternal = MutableStateFlow<LastPick?>(null)
    val lastPick = lastPickInternal.asStateFlow()

    fun load() {
        locationInternal.value = preferences.selectedLocation
        lastPickInternal.value = buildLastPick()

        placesRepository.loadNearby()
        viewModelScope.launch {
            placesRepository.nearby.collect { places ->
                placeCountInternal.value = places.size
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun buildLastPick(): LastPick? {
        val name = preferences.lastPickName ?: return null
        val at = preferences.lastPickAt ?: return null
        val elapsedMs = Clock.System.now().toEpochMilliseconds() - at
        val daysAgo = (elapsedMs / MILLIS_PER_DAY).coerceAtLeast(0L)
        return LastPick(name = name, daysAgo = daysAgo)
    }

    companion object {
        private const val MILLIS_PER_DAY = 1000L * 60 * 60 * 24
    }
}
