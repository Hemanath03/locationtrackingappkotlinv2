package com.example.locationtrackingappv2.domain.usecase

import com.example.locationtrackingappv2.domain.models.PlaceLatLng
import com.example.locationtrackingappv2.domain.repository.DirectionsRepository

class GetRouteUseCase(private val repo: DirectionsRepository) {
    //suspend operator fun invoke(origin: PlaceLatLng, dest: PlaceLatLng) = repo.getRoute(origin,dest)
}