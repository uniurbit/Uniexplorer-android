package com.appload.einkapp.utils.map

import com.google.android.gms.maps.model.LatLng

sealed class DeviceLocation {
    data class Location(val location: LatLng) : DeviceLocation()
    data class Error(val location: LatLng) : DeviceLocation()
}