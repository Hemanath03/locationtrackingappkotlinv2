package com.example.locationtrackingappv2.data.repository

import com.example.locationtrackingappv2.domain.entity.LocationPoint
import com.example.locationtrackingappv2.domain.repository.LocationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-memory LocationRepository implementation.
 *
 * - LocationService should call publishLocation(point) to push updates into the flow.
 * - startTracking/stopTracking are no-ops here; control is from the Service.
 */
@Singleton
class LocationRepositoryImpl @Inject constructor(
) : LocationRepository {

    private var lastPoint: LocationPoint? = null
    private val _locationFlow = MutableSharedFlow<LocationPoint>(replay = 1)
    override fun locationUpdates(): Flow<LocationPoint> = _locationFlow.asSharedFlow()

    private val scope = CoroutineScope(Dispatchers.Default)

    // Called by LocationService to publish a new sample
    suspend fun publishLocation(point: LocationPoint) {
        _locationFlow.emit(point)
    }

    // Optionally expose a quick non-suspending publish (for service convenience)
    fun publishLocationNonBlocking(point: LocationPoint) {
        lastPoint = point
        scope.launch { _locationFlow.emit(point) }
    }

    override fun getLastKnownPoint(): LocationPoint? = lastPoint

    // start/stop are intended to instruct the service; we keep as no-op by default.
    override suspend fun startTracking() { /* service handled externally */ }
    override suspend fun stopTracking() { /* service handled externally */ }
}
