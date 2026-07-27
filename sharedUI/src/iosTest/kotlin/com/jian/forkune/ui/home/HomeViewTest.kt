package com.jian.forkune.ui.home

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test

/**
 * Locks the last-pick footer's copy. The interesting case is 0 days: English ICU plural
 * rules route 0 to `other`, so pulling it from `home_days_ago` renders "0 days ago" —
 * the #39 bug. The View has to special-case it with `home_today`.
 */
@OptIn(ExperimentalTestApi::class)
class HomeViewTest {

    private fun homeView(lastPick: LastPick?) = @androidx.compose.runtime.Composable {
        HomeView(
            location = "Downtown",
            placeCount = 42,
            lastPick = lastPick,
            onSurpriseClicked = {},
            onPickCuisinesClicked = {},
            onFiltersClicked = {},
            onProfileClicked = {},
        )
    }

    @Test
    fun same_day_last_pick_renders_today() = runComposeUiTest {
        setContent(homeView(LastPick(name = "Sushi Zen", daysAgo = 0)))

        onNodeWithText("Last pick: Sushi Zen · today").assertExists()
    }

    @Test
    fun one_day_old_last_pick_renders_singular() = runComposeUiTest {
        setContent(homeView(LastPick(name = "Sushi Zen", daysAgo = 1)))

        onNodeWithText("Last pick: Sushi Zen · 1 day ago").assertExists()
    }

    @Test
    fun older_last_pick_renders_plural() = runComposeUiTest {
        setContent(homeView(LastPick(name = "Sushi Zen", daysAgo = 3)))

        onNodeWithText("Last pick: Sushi Zen · 3 days ago").assertExists()
    }
}
