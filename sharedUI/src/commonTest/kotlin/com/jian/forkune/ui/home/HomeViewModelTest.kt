package com.jian.forkune.ui.home

import com.jian.forkune.data.location.LocationOutcome
import com.jian.forkune.data.location.LocationProvider
import com.jian.forkune.data.places.PlacesRepository
import com.jian.forkune.datamodels.Restaurant
import com.jian.forkune.utilities.preferences.AppPreferences
import com.jian.forkune.utilities.preferences.AppPreferencesInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

private const val MILLIS_PER_HOUR = 60L * 60 * 1000

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun load_populates_location_and_place_count() = runTest(dispatcher) {
        val prefs = AppPreferences(
            FakePreferences(
                strings = mutableMapOf("selectedLocationKey" to "Downtown"),
            ),
        )
        val places = FakePlacesRepository(List(3) { restaurant("Place $it") })
        val viewModel = HomeViewModel(prefs, places, FakeLocationProvider())

        viewModel.load()
        advanceUntilIdle()

        assertEquals("Downtown", viewModel.location.value)
        assertEquals(3, viewModel.placeCount.value)
    }

    @Test
    fun load_reads_last_pick_when_present() = runTest(dispatcher) {
        val prefs = AppPreferences(
            FakePreferences(
                strings = mutableMapOf("lastPickNameKey" to "Sushi Zen"),
                longs = mutableMapOf("lastPickAtKey" to 0L),
            ),
        )
        val viewModel = HomeViewModel(prefs, FakePlacesRepository(emptyList()), FakeLocationProvider())

        viewModel.load()
        advanceUntilIdle()

        assertEquals("Sushi Zen", viewModel.lastPick.value?.name)
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun last_pick_made_earlier_today_is_zero_days_ago() = runTest(dispatcher) {
        // 0 is the boundary the View special-cases as "today" — the plural can't express it
        // (English ICU rules route 0 to `other`, i.e. "0 days ago"). See #39.
        val twoHoursAgo = Clock.System.now().toEpochMilliseconds() - 2 * MILLIS_PER_HOUR
        val viewModel = HomeViewModel(
            AppPreferences(
                FakePreferences(
                    strings = mutableMapOf("lastPickNameKey" to "Sushi Zen"),
                    longs = mutableMapOf("lastPickAtKey" to twoHoursAgo),
                ),
            ),
            FakePlacesRepository(emptyList()),
            FakeLocationProvider(),
        )

        viewModel.load()
        advanceUntilIdle()

        assertEquals(0L, viewModel.lastPick.value?.daysAgo)
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun last_pick_made_yesterday_is_one_day_ago() = runTest(dispatcher) {
        val yesterday = Clock.System.now().toEpochMilliseconds() - 25 * MILLIS_PER_HOUR
        val viewModel = HomeViewModel(
            AppPreferences(
                FakePreferences(
                    strings = mutableMapOf("lastPickNameKey" to "Sushi Zen"),
                    longs = mutableMapOf("lastPickAtKey" to yesterday),
                ),
            ),
            FakePlacesRepository(emptyList()),
            FakeLocationProvider(),
        )

        viewModel.load()
        advanceUntilIdle()

        assertEquals(1L, viewModel.lastPick.value?.daysAgo)
    }

    @Test
    fun load_first_run_has_no_last_pick_and_no_location() = runTest(dispatcher) {
        val prefs = AppPreferences(FakePreferences())
        val viewModel = HomeViewModel(prefs, FakePlacesRepository(emptyList()), FakeLocationProvider())

        viewModel.load()
        advanceUntilIdle()

        assertNull(viewModel.lastPick.value)
        // null location → the View renders the "Choose location" prompt.
        assertNull(viewModel.location.value)
        assertEquals(0, viewModel.placeCount.value)
    }

    @Test
    fun resolving_location_when_granted_persists_area_name_and_returns_true() = runTest(dispatcher) {
        val prefs = AppPreferences(FakePreferences())
        val viewModel = HomeViewModel(
            prefs,
            FakePlacesRepository(emptyList()),
            FakeLocationProvider(LocationOutcome.Resolved("Mission District")),
        )

        viewModel.load()
        advanceUntilIdle()
        assertNull(viewModel.location.value)

        val granted = viewModel.resolveCurrentLocation()
        advanceUntilIdle()

        assertTrue(granted)
        assertEquals("Mission District", viewModel.location.value)
        assertEquals("Mission District", prefs.selectedLocation)
    }

    @Test
    fun load_auto_resolves_location_when_permission_already_granted() = runTest(dispatcher) {
        val prefs = AppPreferences(FakePreferences())
        val viewModel = HomeViewModel(
            prefs,
            FakePlacesRepository(emptyList()),
            FakeLocationProvider(LocationOutcome.Resolved("San Francisco"), hasPermission = true),
        )

        viewModel.load()
        advanceUntilIdle()

        assertEquals("San Francisco", viewModel.location.value)
        assertEquals("San Francisco", prefs.selectedLocation)
    }

    @Test
    fun load_does_not_resolve_location_when_permission_not_granted() = runTest(dispatcher) {
        val prefs = AppPreferences(FakePreferences())
        val viewModel = HomeViewModel(
            prefs,
            FakePlacesRepository(emptyList()),
            FakeLocationProvider(LocationOutcome.Resolved("San Francisco"), hasPermission = false),
        )

        viewModel.load()
        advanceUntilIdle()

        assertNull(viewModel.location.value)
    }

    @Test
    fun resolving_location_when_denied_returns_false_and_keeps_no_location() = runTest(dispatcher) {
        val prefs = AppPreferences(FakePreferences())
        val viewModel = HomeViewModel(
            prefs,
            FakePlacesRepository(emptyList()),
            FakeLocationProvider(LocationOutcome.PermissionDenied),
        )

        viewModel.load()
        advanceUntilIdle()

        val granted = viewModel.resolveCurrentLocation()
        advanceUntilIdle()

        assertFalse(granted)
        assertNull(viewModel.location.value)
        assertNull(prefs.selectedLocation)
    }
}

private fun restaurant(name: String) = Restaurant(
    name = name,
    cuisine = "Sushi",
    rating = 4.5,
    reviewCount = 100,
    priceLevel = 2,
    distanceMi = 0.4,
    isOpenNow = true,
    openUntil = "10:00 PM",
    accentColorArgb = 0xFF356668L,
    initials = "SZ",
)

private class FakeLocationProvider(
    private val outcome: LocationOutcome = LocationOutcome.Resolved(null),
    private val hasPermission: Boolean = false,
) : LocationProvider {
    override fun hasPermission(): Boolean = hasPermission
    override suspend fun resolveCurrentArea(): LocationOutcome = outcome
}

private class FakePlacesRepository(private val initial: List<Restaurant>) : PlacesRepository {
    private val nearbyInternal = MutableStateFlow<List<Restaurant>>(emptyList())
    override val nearby = nearbyInternal.asStateFlow()

    override fun loadNearby(cuisines: Set<String>) {
        nearbyInternal.value = initial
    }
}

private class FakePreferences(
    private val strings: MutableMap<String, String> = mutableMapOf(),
    private val longs: MutableMap<String, Long> = mutableMapOf(),
) : AppPreferencesInterface {
    override fun setString(key: String, value: String) { strings[key] = value }
    override fun getStringOrNull(key: String) = strings[key]
    override fun setLong(key: String, value: Long) { longs[key] = value }
    override fun getLong(key: String, default: Long) = longs[key] ?: default
}
