package com.jian.forkune.data.places

import com.jian.forkune.datamodels.Restaurant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Minimal in-memory [PlacesRepository]. Serves a fixed set of nearby places so the Home
 * screen's place count and the Shuffling candidates have data to show before the real
 * ktor-backed repository (issue #21) lands.
 */
class InMemoryPlacesRepository : PlacesRepository {

    private val nearbyInternal = MutableStateFlow<List<Restaurant>>(emptyList())
    override val nearby = nearbyInternal.asStateFlow()

    override fun loadNearby(cuisines: Set<String>) {
        nearbyInternal.value =
            if (cuisines.isEmpty()) SAMPLE_NEARBY
            else SAMPLE_NEARBY.filter { it.cuisine in cuisines }
    }

    companion object {
        private const val ACCENT_CORAL = 0xFFF4502EL
        private const val ACCENT_TEAL = 0xFF356668L
        private const val ACCENT_BLUE = 0xFF2B6485L

        // A fixed sample of 42 places within a mile — matches the mock's headline count.
        private val CUISINES = listOf(
            "Sushi", "Tacos", "Pizza", "Thai", "Ramen", "Chinese", "Burgers", "Italian",
        )

        val SAMPLE_NEARBY: List<Restaurant> = List(42) { index ->
            val cuisine = CUISINES[index % CUISINES.size]
            val name = "$cuisine Spot ${index + 1}"
            Restaurant(
                name = name,
                cuisine = cuisine,
                rating = 3.8 + (index % 12) * 0.1,
                reviewCount = 40 + index * 7,
                priceLevel = 1 + index % 3,
                distanceMi = 0.1 + (index % 9) * 0.1,
                isOpenNow = index % 5 != 0,
                openUntil = if (index % 5 != 0) "10:00 PM" else null,
                accentColorArgb = when (index % 3) {
                    0 -> ACCENT_CORAL
                    1 -> ACCENT_TEAL
                    else -> ACCENT_BLUE
                },
                initials = cuisine.take(1) + (index + 1).toString(),
            )
        }
    }
}
