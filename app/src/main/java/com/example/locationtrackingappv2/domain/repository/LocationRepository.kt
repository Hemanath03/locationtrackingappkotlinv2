package com.example.locationtrackingappv2.domain.repository

import com.example.locationtrackingappv2.domain.entity.LocationPoint
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    fun locationUpdates(): Flow<LocationPoint>
    suspend fun startTracking()
    suspend fun stopTracking()
}
