package com.jian.forkune.data.location

/**
 * Requests location permission (if needed), resolves the device's current location and
 * reverse-geocodes it to a human-readable area/district name (e.g. "Mission District").
 * Backed by Compass, so a single implementation covers Android and iOS.
 */
interface LocationProvider {
    suspend fun resolveCurrentArea(): LocationOutcome
}

/** Outcome of a [LocationProvider.resolveCurrentArea] call. */
sealed interface LocationOutcome {
    /**
     * Permission was granted. [areaName] is the resolved district name, or null if no fix
     * could be obtained / it couldn't be geocoded (permission is still granted).
     */
    data class Resolved(val areaName: String?) : LocationOutcome

    /** The user denied location permission. */
    data object PermissionDenied : LocationOutcome
}
