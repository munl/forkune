package com.jian.forkune.data.places

import com.jian.forkune.datamodels.Restaurant
import kotlinx.coroutines.flow.StateFlow

/**
 * Supplies nearby places (the "42 places within a mile" set), optionally filtered by
 * selected cuisines. Exposed as an interface so ViewModels can be tested against a fake.
 *
 * NOTE (issue #21, Shuffling epic): the real implementation is ktor-backed and maps a
 * network DTO ↔ [Restaurant] at its boundary. [InMemoryPlacesRepository] is the minimal
 * in-memory stand-in that unblocks the Home screen (#9); Shuffling replaces it later.
 */
interface PlacesRepository {
    val nearby: StateFlow<List<Restaurant>>

    /** Populate [nearby] with places matching [cuisines] (empty = all cuisines). */
    fun loadNearby(cuisines: Set<String> = emptySet())
}
