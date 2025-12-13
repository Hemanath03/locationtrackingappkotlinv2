package com.example.locationtrackingappv2.domain.usecase

import com.example.locationtrackingappv2.domain.repository.LocationRepository
import javax.inject.Inject

class StartTrackingUseCase @Inject constructor(
    private val repo: LocationRepository
) {
    suspend operator fun invoke() = repo.startTracking()
}
