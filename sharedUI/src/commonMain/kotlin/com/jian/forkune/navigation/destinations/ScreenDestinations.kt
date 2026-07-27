package com.jian.forkune.navigation.destinations

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

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

/**
 * Lets `rememberNavBackStack` save and restore the back stack across configuration changes
 * and process death. [NavKey] is an open polymorphic base, so on non-Android targets every
 * subtype must be registered explicitly — Android's reflection-based overload isn't
 * available in commonMain.
 *
 * **Add every new [ScreenDestinations] entry above to this list too**, or restoring a back
 * stack containing it fails at runtime.
 */
val navSavedStateConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(ScreenDestinations.Home::class, ScreenDestinations.Home.serializer())
            subclass(
                ScreenDestinations.PickCuisines::class,
                ScreenDestinations.PickCuisines.serializer(),
            )
            subclass(ScreenDestinations.Filters::class, ScreenDestinations.Filters.serializer())
            subclass(ScreenDestinations.Shuffling::class, ScreenDestinations.Shuffling.serializer())
        }
    }
}
