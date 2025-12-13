package com.example.locationtrackingappv2.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.locationtrackingappv2.domain.entity.LocationPoint
import com.example.locationtrackingappv2.domain.entity.LocationStats
import com.example.locationtrackingappv2.domain.entity.PlaceSuggestion
import com.example.locationtrackingappv2.domain.repository.DirectionsRepository
import com.example.locationtrackingappv2.domain.repository.GoogleMapsRepository
import com.example.locationtrackingappv2.domain.usecase.ObserveLocationStatsUseCase
import com.example.locationtrackingappv2.domain.usecase.ObserveLocationUseCase
import com.example.locationtrackingappv2.data.repository.SnapToRoadRepository
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrackingViewModel @Inject constructor(
    observeLocationUseCase: ObserveLocationUseCase,
    private val observeStats: ObserveLocationStatsUseCase,
    private val placesRepo: GoogleMapsRepository,
    private val directionsRepository: DirectionsRepository,
    private val snapRepo: SnapToRoadRepository
) : ViewModel() {

    // ************************************
    // LIVE UI STATE
    // ************************************

    private val _fare = MutableStateFlow(0.0)
    val fare = _fare.asStateFlow()

    private val _estimatedFare = MutableStateFlow<Double?>(null)
    val estimatedFare = _estimatedFare.asStateFlow()

    private val _currentLocation = MutableStateFlow<LocationPoint?>(null)
    val currentLocation = _currentLocation.asStateFlow()

    private val _points = MutableStateFlow<List<LocationPoint>>(emptyList())
    val points = _points.asStateFlow()

    private val _stats = MutableStateFlow<LocationStats?>(null)
    val stats = _stats.asStateFlow()

    private val _meterRunning = MutableStateFlow(false)
    val meterRunning = _meterRunning.asStateFlow()

    private val _destination = MutableStateFlow<LocationPoint?>(null)
    val destination = _destination.asStateFlow()

    private val _destSuggestions = MutableStateFlow<List<PlaceSuggestion>>(emptyList())
    val destSuggestions = _destSuggestions.asStateFlow()

    private val _fullRoute = MutableStateFlow<List<LatLng>>(emptyList())
    val fullRoute = _fullRoute.asStateFlow()

    private val _remainingRoute = MutableStateFlow<List<LatLng>>(emptyList())
    val remainingRoute = _remainingRoute.asStateFlow()

    private val _routeDistance = MutableStateFlow<Int?>(null)
    val routeDistance = _routeDistance.asStateFlow()

    private val _routeDuration = MutableStateFlow<Int?>(null)
    val routeDuration = _routeDuration.asStateFlow()

    private val _remainingDistance = MutableStateFlow<Double?>(null)
    val remainingDistance = _remainingDistance.asStateFlow()

    // ************************************
    // INIT: start collecting GPS + stats
    // ************************************
    init {
        // GPS updates from service
        viewModelScope.launch {
            observeLocationUseCase().collect { raw ->
                val snapped = snapRepo.snap(raw)
                _currentLocation.value = snapped

                if (_meterRunning.value) {
                    _points.value = _points.value + snapped
                }

                trimRemainingRouteToCurrentPosition(LatLng(snapped.lat, snapped.lng))
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

    // ************************************
    // METER CONTROL
    // ************************************

    fun startMeter() {
        _points.value = emptyList()
        _stats.value = null
        _remainingDistance.value = null
        _fare.value = 0.0
        _meterRunning.value = true
    }

    fun stopMeter() {
        _meterRunning.value = false
        resetAllExceptFare()
    }

    private fun updateFare(stats: LocationStats) {
        val km = stats.totalDistanceMeters / 1000.0
        val speed = stats.instantSpeedMps * 3.6
        val rate = if (speed > 25.0) 3.0 else 2.0
        _fare.value = km * rate
    }

    // ************************************
    // DESTINATION SEARCH + SELECTION
    // ************************************

    fun searchPlaces(query: String) {
        if (query.isBlank()) {
            _destSuggestions.value = emptyList()
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val preds = placesRepo.getPlaceSuggestions(query)
            _destSuggestions.value =
                preds.map { PlaceSuggestion(it.placeId, it.description) }
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

            val origin = _currentLocation.value
            if (origin != null) {
                loadRoute(origin, coords)
            }
            // Else → NO FusedLocationProvider fallback
            // UI waits for first GPS fix before allowing selection
        }
    }

    // ************************************
    // ROUTE LOADING + PROCESSING
    // ************************************

    fun loadRoute(origin: LocationPoint, dest: LocationPoint) {
        viewModelScope.launch(Dispatchers.IO) {
            val (poly, dist, dur) = directionsRepository.getRoutePoints(origin, dest)

            if (poly.isNotEmpty()) {
                _fullRoute.value = poly
                _remainingRoute.value = poly
                _routeDistance.value = dist
                _routeDuration.value = dur
                _estimatedFare.value = (dist / 1000.0) * 3.0
            }
        }
    }

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

        _remainingRoute.value = route.drop(startIndex)
    }

    private fun updateRemainingDistance(cur: LocationPoint?) {
        cur ?: return
        val route = _remainingRoute.value
        if (route.isEmpty()) {
            _remainingDistance.value = null
            return
        }

        var closest = 0
        var min = Float.MAX_VALUE
        val f = FloatArray(1)
        val here = LatLng(cur.lat, cur.lng)

        for (i in route.indices) {
            android.location.Location.distanceBetween(
                here.latitude, here.longitude,
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
                route[i + 1].latitude, route[i + 1].longitude,
                f
            )
            total += f[0]
        }

        _remainingDistance.value = total
    }

    private fun resetAllExceptFare() {
        _points.value = emptyList()
        _stats.value = null
        _remainingDistance.value = null
        clearDestination()
    }
}
