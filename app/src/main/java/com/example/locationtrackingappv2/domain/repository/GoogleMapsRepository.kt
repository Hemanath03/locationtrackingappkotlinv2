package com.example.locationtrackingappv2.domain.repository

import com.example.locationtrackingappv2.domain.models.PlaceLatLng
import com.example.locationtrackingappv2.domain.models.Prediction
import com.example.locationtrackingappv2.domain.models.RouteInfo

interface GoogleMapsRepository {
    suspend fun getPlaceSuggestions(query: String, sessionToken: String): List<Prediction>
    suspend fun getPlaceCoordinates(placeId: String, sessionToken: String): Prediction?
    suspend fun computeRoutes(origin: PlaceLatLng, destination: PlaceLatLng): RouteInfo
}