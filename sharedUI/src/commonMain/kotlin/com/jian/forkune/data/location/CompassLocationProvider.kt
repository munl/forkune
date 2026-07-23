package com.jian.forkune.data.location

import dev.jordond.compass.Place
import dev.jordond.compass.geocoder.Geocoder
import dev.jordond.compass.geocoder.MobileGeocoder
import dev.jordond.compass.geolocation.Geolocator
import dev.jordond.compass.geolocation.GeolocatorResult
import dev.jordond.compass.geolocation.MobileGeolocator

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
    private val geolocator: Geolocator = MobileGeolocator(),
    private val geocoder: Geocoder = MobileGeocoder(),
) : LocationProvider {

    override suspend fun resolveCurrentArea(): LocationOutcome =
        when (val result = geolocator.current()) {
            is GeolocatorResult.Success -> {
                val coordinates = result.data.coordinates
                val place = geocoder
                    .reverse(coordinates.latitude, coordinates.longitude)
                    .getFirstOrNull()
                LocationOutcome.Resolved(place?.districtName())
            }
            is GeolocatorResult.PermissionDenied -> LocationOutcome.PermissionDenied
            // Permission was granted but no fix / unsupported — keep the flow unblocked.
            is GeolocatorResult.Error -> LocationOutcome.Resolved(null)
        }

    private fun Place.districtName(): String? =
        subLocality ?: locality ?: subAdministrativeArea ?: administrativeArea
}
