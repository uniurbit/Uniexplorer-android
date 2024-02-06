package com.appload.einkapp.view.explore

import android.util.Log
import org.altbeacon.beacon.Beacon

private const val MAX_SIZE = 1

class BeaconWithTimer(var beacon: Beacon, var lastSeen: Long, var firstSeen: Long) {

    var latestDistances = ArrayList<Double>()//Ultime 10 distanze rilevate

    init {
        if (latestDistances.size < MAX_SIZE)
            latestDistances.add(beacon.distance)
    }

     fun calculateDistance(txPower: Int, rssi: Int): Double {
        if (rssi == 0) {
            return -1.0 // if we cannot determine distance, return -1.
        }
        val ratio = rssi * 1.0 / txPower
        return if (ratio < 1.0) {
            Math.pow(ratio, 10.0)
        } else {
            0.89976 * Math.pow(ratio, 7.7095) + 0.111
        }
    }

    fun updateLatestDistances(distance: Double?) {
        if (distance == null || distance < 0) return
        if (latestDistances.size == MAX_SIZE)
            latestDistances.removeAt(0)

        latestDistances.add(distance)
    }

    fun avgDistances(): Double {
        return try {
//            if(latestDistances.remainingCapacity() == 0)//Ritorno la media solo se la lista è piena quindi ho una stima decente
            latestDistances.average()
//            else
//                1.0//Altrimenti niente
        } catch (e: java.lang.Exception) {
            0.0
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BeaconWithTimer

        if (beacon != other.beacon) return false

        return true
    }

    override fun hashCode(): Int {
        return beacon.hashCode()
    }

    override fun toString(): String {
        return "BeaconWithTimer(beacon=${beacon.rssi},tx=${beacon.txPower}, lastSeen=$lastSeen, firstSeen=$firstSeen, latestDistances=$latestDistances)"
    }


}