package com.appload.einkapp.utils.map

import android.annotation.SuppressLint
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng

/**
 * Needs to call stopUpdates on context stop
 */
class LocationUpdates(
    private val fusedLocationProviderClient: FusedLocationProviderClient,
    val result: (DeviceLocation) -> Unit
) {

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(location: LocationResult) {
            if (location?.lastLocation != null) {
                result(
                    DeviceLocation.Location(
                        LatLng(
                            location.lastLocation!!.latitude,
                            location.lastLocation!!.longitude
                        )
                    )
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun requestLocationUpdates() {
        val locationRequest = LocationRequest.Builder(5000)
            .setMinUpdateDistanceMeters(5f)
            .build()
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    fun stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }
}