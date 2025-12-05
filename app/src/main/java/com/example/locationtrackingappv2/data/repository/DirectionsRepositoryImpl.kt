package com.example.locationtrackingappv2.data.repository

import com.example.locationtrackingappv2.domain.models.PlaceLatLng
import com.example.locationtrackingappv2.domain.models.RouteInfo
import com.example.locationtrackingappv2.domain.repository.DirectionsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DirectionsRepositoryImpl @Inject constructor(
    private val googleRepo: GoogleMapsRepositoryImpl
) : DirectionsRepository {

    override suspend fun computeRoute(origin: PlaceLatLng, dest: PlaceLatLng): RouteInfo {
        return googleRepo.computeRoutes(origin, dest)
    }
}
