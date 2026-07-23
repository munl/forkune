package com.jian.forkune.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.compose.viewmodel.koinViewModel

/**
 * Home entry/wiring layer (clean-architecture §5). Obtains [HomeViewModel] via Koin,
 * collects its flows, triggers [HomeViewModel.load] once, and forwards state + the
 * navigation lambdas (supplied by the nav graph) down to the stateless [HomeView].
 * No business logic here.
 */
@Composable
fun HomeScreen(
    onSurpriseClicked: () -> Unit,
    onPickCuisinesClicked: () -> Unit,
    onFiltersClicked: () -> Unit,
    onProfileClicked: () -> Unit,
) {
    val viewModel = koinViewModel<HomeViewModel>()

    LaunchedEffect(Unit) { viewModel.load() }

    val location by viewModel.location.collectAsState()
    val placeCount by viewModel.placeCount.collectAsState()
    val lastPick by viewModel.lastPick.collectAsState()

    HomeView(
        location = location,
        placeCount = placeCount,
        lastPick = lastPick,
        onSurpriseClicked = onSurpriseClicked,
        onPickCuisinesClicked = onPickCuisinesClicked,
        onFiltersClicked = onFiltersClicked,
        onProfileClicked = onProfileClicked,
    )
}
