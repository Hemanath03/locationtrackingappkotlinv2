package com.example.locationtrackingappv2.data.repository

import com.example.locationtrackingappv2.BuildConfig
import com.example.locationtrackingappv2.data.remote.RoadsApi
import com.example.locationtrackingappv2.domain.entity.LocationPoint
import javax.inject.Inject

class SnapToRoadRepository @Inject constructor(
    private val api: RoadsApi
) {

    suspend fun snap(point: LocationPoint): LocationPoint {
        return try {
            val response = api.snapToRoad(
                latLng = "${point.lat},${point.lng}",
                apiKey = BuildConfig.MAPS_API_KEY
            )

            val snapped = response.snappedPoints?.firstOrNull()?.location
                ?: return point  // fallback to raw GPS

            LocationPoint(
                lat = snapped.latitude,
                lng = snapped.longitude,
                timestamp = point.timestamp
            )
        } catch (e: Exception) {
            // fallback to raw GPS if API fails
            point
        }
    }
}
