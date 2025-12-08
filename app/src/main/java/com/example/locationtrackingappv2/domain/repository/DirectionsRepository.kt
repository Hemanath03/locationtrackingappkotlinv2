package com.example.locationtrackingappv2.domain.repository

import com.example.locationtrackingappv2.domain.entity.LocationPoint
import com.google.android.gms.maps.model.LatLng

interface DirectionsRepository {
    suspend fun getRoutePoints(origin: LocationPoint, dest: LocationPoint):Triple<List<LatLng>, Int, Int>
}
