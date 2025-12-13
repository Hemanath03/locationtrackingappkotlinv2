package com.example.locationtrackingappv2.utils

import com.example.locationtrackingappv2.domain.entity.LocationPoint
import android.location.Location

object DistanceCalculator {

    /**
     * Returns distance in meters between two LocationPoints.
     */
    fun distanceBetweenMeters(a: LocationPoint, b: LocationPoint): Double {
        val results = FloatArray(1)
        Location.distanceBetween(a.lat, a.lng, b.lat, b.lng, results)
        return results[0].toDouble()
    }
}
