package com.example.locationtrackingappv2.domain.repository

import com.example.locationtrackingappv2.domain.entity.LocationPoint
import com.example.locationtrackingappv2.domain.entity.PlaceSuggestion

interface GoogleMapsRepository {
    suspend fun getPlaceSuggestions(query: String): List<PlaceSuggestion>
    suspend fun getPlaceCoordinates(placeId: String): LocationPoint
}
