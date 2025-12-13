package com.example.locationtrackingappv2.domain.usecase

import com.example.locationtrackingappv2.domain.repository.LocationRepository
import javax.inject.Inject

class StopTrackingUseCase @Inject constructor(
    private val repo: LocationRepository
) {
    suspend operator fun invoke() = repo.stopTracking()
}
