package com.jian.forkune.navigation.destinations

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Navigation3 keys for the app's screens (clean-architecture §8). Each is a `@Serializable`
 * [NavKey]; screens taking arguments become `data class`es with serializable-primitive
 * fields as their epics land (e.g. Shuffling's selected cuisines in #26).
 */
sealed interface ScreenDestinations : NavKey {

    /** App start destination. */
    @Serializable
    data object Home : ScreenDestinations

    @Serializable
    data object PickCuisines : ScreenDestinations

    @Serializable
    data object Filters : ScreenDestinations

    @Serializable
    data object Shuffling : ScreenDestinations
}
