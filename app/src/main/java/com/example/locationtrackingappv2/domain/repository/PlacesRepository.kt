package com.example.locationtrackingappv2.domain.repository

import com.example.locationtrackingappv2.domain.models.PlaceLatLng
import com.example.locationtrackingappv2.domain.models.Prediction

interface PlacesRepository {
    suspend fun getPredictions(query: String, country: String = "AU"): List<Prediction>
    suspend fun fetchPlaceLatLng(placeId: String): PlaceLatLng?
}