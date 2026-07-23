package com.jian.forkune.data.location

import dev.jordond.compass.Place
import dev.jordond.compass.geocoder.Geocoder
import dev.jordond.compass.geocoder.MobileGeocoder
import dev.jordond.compass.geolocation.Geolocator
import dev.jordond.compass.geolocation.GeolocatorResult
import dev.jordond.compass.geolocation.MobileGeolocator
import dev.jordond.compass.permissions.LocationPermissionController
import dev.jordond.compass.permissions.MobileLocationPermissionController

/**
 * Compass-backed [LocationProvider] — one implementation for both Android and iOS.
 *
 * [Geolocator.mobile] uses Compass's default location-permission controller (which
 * auto-tracks the current Android Activity / iOS authorization), so requesting the current
 * location also drives the permission prompt. [Geocoder.mobile] reverse-geocodes via the
 * native platform geocoder. Requires the location permissions in the Android manifest and
 * NSLocationWhenInUseUsageDescription in the iOS Info.plist.
 */
class CompassLocationProvider(
    private val permissionController: LocationPermissionController = MobileLocationPermissionController(),
    private val geolocator: Geolocator = MobileGeolocator(permissionController),
    private val geocoder: Geocoder = MobileGeocoder(),
) : LocationProvider {

    override fun hasPermission(): Boolean = permissionController.hasPermission()

    override suspend fun resolveCurrentArea(): LocationOutcome =
        when (val result = geolocator.current()) {
            is GeolocatorResult.Success -> {
                val coordinates = result.data.coordinates
                val place = geocoder
                    .reverse(coordinates.latitude, coordinates.longitude)
                    .getFirstOrNull()
                LocationOutcome.Resolved(place?.cityName())
            }
            is GeolocatorResult.PermissionDenied -> LocationOutcome.PermissionDenied
            // Permission was granted but no fix / unsupported — keep the flow unblocked.
            is GeolocatorResult.Error -> LocationOutcome.Resolved(null)
        }

    // Prefer the city (locality); fall back to neighbourhood, then county/state.
    private fun Place.cityName(): String? =
        locality ?: subLocality ?: subAdministrativeArea ?: administrativeArea
}
