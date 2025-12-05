package com.example.locationtrackingappv2.domain.usecase

import com.example.locationtrackingappv2.domain.models.PlaceLatLng
import com.example.locationtrackingappv2.domain.models.RouteInfo
import com.example.locationtrackingappv2.domain.repository.GoogleMapsRepository
import javax.inject.Inject

class ComputeRoutesUseCase @Inject constructor(
    private val repo: GoogleMapsRepository
) {
    suspend operator fun invoke(origin: PlaceLatLng, dest: PlaceLatLng): RouteInfo {
        return repo.computeRoutes(origin, dest)
    }
}
