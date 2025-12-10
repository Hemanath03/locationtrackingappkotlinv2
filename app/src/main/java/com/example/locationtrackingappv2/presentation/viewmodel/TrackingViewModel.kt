// ==========================
// TrackingViewModel.kt (Full Updated with Snap-to-Road + FullRoute + RemainingRoute)
// ==========================
package com.example.locationtrackingappv2.presentation.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.locationtrackingappv2.domain.entity.LocationPoint
import com.example.locationtrackingappv2.domain.entity.LocationStats
import com.example.locationtrackingappv2.domain.repository.DirectionsRepository
import com.example.locationtrackingappv2.domain.repository.GoogleMapsRepository
import com.example.locationtrackingappv2.domain.usecase.ObserveLocationStatsUseCase
import com.example.locationtrackingappv2.domain.usecase.ObserveLocationUseCase
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import com.example.locationtrackingappv2.data.repository.SnapToRoadRepository
import com.example.locationtrackingappv2.domain.entity.PlaceSuggestion
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel
class TrackingViewModel @Inject constructor(
    observeLocationUseCase: ObserveLocationUseCase,
    private val observeStats: ObserveLocationStatsUseCase,
    private val placesRepo: GoogleMapsRepository,
    private val directionsRepository: DirectionsRepository,
    private val snapRepo: SnapToRoadRepository
) : ViewModel() {

    private val TAG = "TrackingViewModel"

    // Live snapped GPS location
    private val _currentLocation = MutableStateFlow<LocationPoint?>(null)
    val currentLocation = _currentLocation.asStateFlow()

    // Driver path when meter running
    private val _points = MutableStateFlow<List<LocationPoint>>(emptyList())
    val points = _points.asStateFlow()

    // Stats
    private val _stats = MutableStateFlow<LocationStats?>(null)
    val stats = _stats.asStateFlow()

    // Meter
    private val _meterRunning = MutableStateFlow(false)
    val meterRunning = _meterRunning.asStateFlow()

    private val _fare = MutableStateFlow(0.0)
    val fare = _fare.asStateFlow()

    // Destination
    private val _destination = MutableStateFlow<LocationPoint?>(null)
    val destination = _destination.asStateFlow()

    private val _destSuggestions = MutableStateFlow<List<PlaceSuggestion>>(emptyList())
    val destSuggestions = _destSuggestions.asStateFlow()

    // Full route (always intact)
    private val _fullRoute = MutableStateFlow<List<LatLng>>(emptyList())
    val fullRoute = _fullRoute.asStateFlow()

    // Remaining route (trimmed visually)
    private val _remainingRoute = MutableStateFlow<List<LatLng>>(emptyList())
    val remainingRoute = _remainingRoute.asStateFlow()

    private val _routeDistance = MutableStateFlow<Int?>(null)
    val routeDistance = _routeDistance.asStateFlow()

    private val _routeDuration = MutableStateFlow<Int?>(null)
    val routeDuration = _routeDuration.asStateFlow()

    private val _remainingDistance = MutableStateFlow<Double?>(null)
    val remainingDistance = _remainingDistance.asStateFlow()

    private val _estimatedFare = MutableStateFlow<Double?>(null)
    val estimatedFare = _estimatedFare.asStateFlow()


    init {
        // GPS updates
        viewModelScope.launch {
            observeLocationUseCase().collect { rawLp ->

                // Snap to road
                val snapped = snapRepo.snap(rawLp)
                _currentLocation.value = snapped

                if (_meterRunning.value) {
                    _points.value = _points.value + snapped
                }

                // Trim route visually
                trimRemainingRouteToCurrentPosition(LatLng(snapped.lat, snapped.lng))

                // Update remaining distance
                updateRemainingDistance(snapped)
            }
        }

        // Stats updates
        viewModelScope.launch {
            observeStats().collect { s ->
                _stats.value = s
                if (_meterRunning.value) updateFare(s)
            }
        }
    }

    // =============================
    // Meter
    // =============================
    fun startMeter() {
        _points.value = emptyList()
        _fare.value = 0.0
        _meterRunning.value = true
    }

    fun stopMeter() {
        _meterRunning.value = false
    }

    private fun updateFare(stats: LocationStats) {
        val km = stats.totalDistanceMeters / 1000.0
        val speed = stats.instantSpeedMps * 3.6
        val rate = if (speed > 25.0) 3.0 else 2.0
        _fare.value = km * rate
    }

    // =============================
    // Places autocomplete
    // =============================

    fun searchPlaces(q: String) {
        if (q.isBlank()) {
            _destSuggestions.value = emptyList()
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val preds = placesRepo.getPlaceSuggestions(q)
            _destSuggestions.value = preds.map { PlaceSuggestion(it.placeId, it.description) }
        }
    }

    fun clearDestination() {
        _destination.value = null
        _destSuggestions.value = emptyList()
        _fullRoute.value = emptyList()
        _remainingRoute.value = emptyList()
        _routeDistance.value = null
        _routeDuration.value = null
        _estimatedFare.value = null
        _remainingDistance.value = null
    }

    fun onPlaceSelected(s: PlaceSuggestion, ctx: Context) {
        viewModelScope.launch {
            val coords = placesRepo.getPlaceCoordinates(s.placeId)
            _destination.value = coords

            val origin = _currentLocation.value ?: getImmediateOriginFallback(ctx)
            if (origin != null) loadRoute(origin, coords)
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun getImmediateOriginFallback(ctx: Context): LocationPoint? {
        return try {
            val fused = LocationServices.getFusedLocationProviderClient(ctx)

            val fresh = fused.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
            if (fresh != null) return LocationPoint(fresh.latitude, fresh.longitude, System.currentTimeMillis())

            val last = fused.lastLocation.await()
            if (last != null) return LocationPoint(last.latitude, last.longitude, System.currentTimeMillis())

            null
        } catch (e: Exception) { null }
    }

    // =============================
    // Route loading
    // =============================
    fun loadRoute(origin: LocationPoint, dest: LocationPoint) {
        viewModelScope.launch(Dispatchers.IO) {
            val (poly, distance, duration) = directionsRepository.getRoutePoints(origin, dest)

            if (poly.isNotEmpty()) {
                _fullRoute.value = poly.toList()       // store full
                _remainingRoute.value = poly.toList()  // initial remaining
                _routeDistance.value = distance
                _routeDuration.value = duration
                _estimatedFare.value = (distance / 1000.0) * 3.0
            }
        }
    }

    // =============================
    // Trim remaining route
    // =============================
    private fun trimRemainingRouteToCurrentPosition(current: LatLng, threshold: Float = 30f) {
        val route = _remainingRoute.value
        if (route.isEmpty()) return

        var closestIndex = 0
        var minDist = Float.MAX_VALUE
        val results = FloatArray(1)

        for (i in route.indices) {
            android.location.Location.distanceBetween(
                current.latitude, current.longitude,
                route[i].latitude, route[i].longitude,
                results
            )
            if (results[0] < minDist) {
                minDist = results[0]
                closestIndex = i
            }
        }

        if (minDist > threshold) return

        val startIndex = (closestIndex - 1).coerceAtLeast(0)

        if (startIndex >= route.size - 1) {
            _remainingRoute.value = emptyList()
            _remainingDistance.value = 0.0
            return
        }

        val trimmed = route.drop(startIndex)
        _remainingRoute.value = trimmed
    }

    // =============================
    // Remaining distance
    // =============================
    private fun updateRemainingDistance(current: LocationPoint?) {
        current ?: return
        val route = _remainingRoute.value
        if (route.isEmpty()) {
            _remainingDistance.value = null
            return
        }

        val cur = LatLng(current.lat, current.lng)
        var closest = 0
        var min = Float.MAX_VALUE
        val f = FloatArray(1)

        for (i in route.indices) {
            android.location.Location.distanceBetween(
                cur.latitude, cur.longitude,
                route[i].latitude, route[i].longitude,
                f
            )
            if (f[0] < min) {
                min = f[0]
                closest = i
            }
        }

        var total = 0.0
        for (i in closest until route.size - 1) {
            android.location.Location.distanceBetween(
                route[i].latitude, route[i].longitude,
                route[i+1].latitude, route[i+1].longitude,
                f
            )
            total += f[0]
        }

        _remainingDistance.value = total
    }
}

