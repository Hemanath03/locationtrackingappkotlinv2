package com.example.locationtrackingappv2.domain.usecase

import com.example.locationtrackingappv2.domain.entity.LocationPoint
import com.example.locationtrackingappv2.domain.repository.LocationRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveLocationUseCase @Inject constructor(
    private val repo: LocationRepository
) {
    operator fun invoke(): Flow<LocationPoint> = repo.locationUpdates()
}
