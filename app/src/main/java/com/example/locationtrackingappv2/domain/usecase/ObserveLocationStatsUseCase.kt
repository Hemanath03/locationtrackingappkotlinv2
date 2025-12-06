package com.example.locationtrackingappv2.domain.usecase

import com.example.locationtrackingappv2.domain.entity.LocationPoint
import com.example.locationtrackingappv2.domain.entity.LocationStats
import com.example.locationtrackingappv2.domain.repository.LocationRepository
import com.example.locationtrackingappv2.utils.DistanceCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.transform
import javax.inject.Inject

/**
 * Produces a stream of LocationStats computed incrementally from raw location updates.
 */
class ObserveLocationStatsUseCase @Inject constructor(
    private val locationRepository: LocationRepository
) {
    operator fun invoke(): Flow<LocationStats> = flow {
        var prev: LocationPoint? = null
        var totalDistance = 0.0
        var startTime: Long? = null

        locationRepository.locationUpdates().collect { point ->
            if (startTime == null) startTime = point.timestamp

            val instantSpeed: Double = prev?.let { p ->
                val meters = DistanceCalculator.distanceBetweenMeters(p, point)
                totalDistance += meters
                val dtSeconds = (point.timestamp - p.timestamp) / 1000.0
                if (dtSeconds > 0.0) meters / dtSeconds else 0.0
            } ?: 0.0

            val elapsedSeconds = ((point.timestamp - (startTime ?: point.timestamp)) / 1000.0).coerceAtLeast(0.0)
            val avgSpeed = if (elapsedSeconds > 0.0) totalDistance / elapsedSeconds else 0.0

            emit(LocationStats(last = point, totalDistanceMeters = totalDistance, instantSpeedMps = instantSpeed, averageSpeedMps = avgSpeed))
            prev = point
        }
    }
}
