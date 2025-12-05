package com.example.locationtrackingappv2.domain.repository

import com.example.locationtrackingappv2.domain.models.PlaceLatLng
import com.example.locationtrackingappv2.domain.models.RouteInfo

interface DirectionsRepository {
    suspend fun computeRoute(origin: PlaceLatLng, dest: PlaceLatLng): RouteInfo
}
