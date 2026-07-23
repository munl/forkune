package com.jian.forkune.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateListOf
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.jian.forkune.navigation.destinations.ScreenDestinations
import com.jian.forkune.ui.home.HomeScreen
import com.jian.forkune.ui.placeholder.PlaceholderScreen

/**
 * Root Navigation3 graph (clean-architecture §8). Home is the start destination; each
 * `entry<...>` renders a screen and is handed the navigation lambdas it needs. Screens
 * from later epics are rendered by [PlaceholderScreen] until they land.
 */
@Composable
fun MainNavGraph() {
    val backStack = remember { mutableStateListOf<NavKey>(ScreenDestinations.Home) }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<ScreenDestinations.Home> {
                HomeScreen(
                    onSurpriseClicked = { backStack.add(ScreenDestinations.Shuffling) },
                    onPickCuisinesClicked = { backStack.add(ScreenDestinations.PickCuisines) },
                    onFiltersClicked = { backStack.add(ScreenDestinations.Filters) },
                    onProfileClicked = { /* Profile screen not part of this epic. */ },
                )
            }
            entry<ScreenDestinations.PickCuisines> {
                PlaceholderScreen(title = "Pick cuisines")
            }
            entry<ScreenDestinations.Filters> {
                PlaceholderScreen(title = "Filters")
            }
            entry<ScreenDestinations.Shuffling> {
                PlaceholderScreen(title = "Shuffling")
            }
        },
    )
}
