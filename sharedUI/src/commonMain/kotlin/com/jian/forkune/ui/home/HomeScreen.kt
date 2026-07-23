package com.jian.forkune.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

/**
 * Home entry/wiring layer (clean-architecture §5). Obtains [HomeViewModel] via Koin,
 * collects its flows, and owns the location flow: tapping the location chip or "Surprise me"
 * calls [HomeViewModel.resolveCurrentLocation], which requests LOCATION permission (Compass)
 * and resolves the area name. "Surprise me" only proceeds if permission was granted; denial
 * keeps the user on Home. No business logic here beyond wiring.
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

    val scope = rememberCoroutineScope()

    // Requests location; runs [onGranted] only if permission was granted. Denial blocks.
    fun requestLocationThen(onGranted: () -> Unit) {
        scope.launch {
            if (viewModel.resolveCurrentLocation()) onGranted()
        }
    }

    val location by viewModel.location.collectAsState()
    val placeCount by viewModel.placeCount.collectAsState()
    val lastPick by viewModel.lastPick.collectAsState()

    HomeView(
        location = location,
        placeCount = placeCount,
        lastPick = lastPick,
        onSurpriseClicked = { requestLocationThen(onSurpriseClicked) },
        onPickCuisinesClicked = onPickCuisinesClicked,
        onFiltersClicked = onFiltersClicked,
        onProfileClicked = onProfileClicked,
        onLocationClicked = { requestLocationThen {} },
    )
}
