package com.jian.forkune.navigation.destinations

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.serialization.NavBackStackSerializer
import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState
import kotlinx.serialization.PolymorphicSerializer
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Guards [navSavedStateConfiguration]: `rememberNavBackStack` only exercises it when Android
 * restores a saved back stack, so an unregistered destination would otherwise stay invisible
 * until it crashed in the field. Add a destination without registering it and this fails.
 */
class ScreenDestinationsSerializationTest {

    private val serializer = NavBackStackSerializer(PolymorphicSerializer(NavKey::class))

    private fun roundTrip(vararg keys: NavKey): List<NavKey> {
        val encoded = encodeToSavedState(
            serializer = serializer,
            value = NavBackStack(*keys),
            configuration = navSavedStateConfiguration,
        )
        // .toList(): NavBackStack delegates MutableList but doesn't override equals, so
        // comparing it directly against a List is an identity check and always fails.
        return decodeFromSavedState(
            deserializer = serializer,
            savedState = encoded,
            configuration = navSavedStateConfiguration,
        ).toList()
    }

    @Test
    fun everyDestinationSurvivesSaveAndRestore() {
        val all = listOf<NavKey>(
            ScreenDestinations.Home,
            ScreenDestinations.PickCuisines,
            ScreenDestinations.Filters,
            ScreenDestinations.Shuffling,
        )

        assertEquals(all, roundTrip(*all.toTypedArray()))
    }

    @Test
    fun restoresAMultiEntryStackInOrder() {
        val stack = listOf<NavKey>(ScreenDestinations.Home, ScreenDestinations.Filters)

        assertEquals(stack, roundTrip(*stack.toTypedArray()))
    }
}
