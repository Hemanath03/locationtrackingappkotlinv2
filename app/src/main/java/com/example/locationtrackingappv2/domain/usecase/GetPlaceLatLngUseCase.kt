package com.example.locationtrackingappv2.domain.usecase

import com.example.locationtrackingappv2.domain.models.Prediction
import com.example.locationtrackingappv2.domain.repository.GoogleMapsRepository
import javax.inject.Inject

class GetPlaceLatLngUseCase @Inject constructor(
    private val repo: GoogleMapsRepository
) {
    suspend operator fun invoke(placeId: String, sessionToken: String): Prediction? {
        return repo.getPlaceCoordinates(placeId, sessionToken)
    }
}
