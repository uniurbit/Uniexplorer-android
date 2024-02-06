package com.appload.einkapp.model.internal

data class SnoozedBeacons(
    var beacons: ArrayList<BeaconInfo>
) {
    data class BeaconInfo(
        val uuid: String,
        val major: String,
        val minor: String,
        var lastTimeSnoozed: Long
    )
}
