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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class PlaceSuggestionUi(
    val placeId: String,
    val description: String
)

@HiltViewModel
class TrackingViewModel @Inject constructor(
    observeLocationUseCase: ObserveLocationUseCase,
    private val observeStats: ObserveLocationStatsUseCase,
    private val placesRepo: GoogleMapsRepository,
    private val directionsRepository: DirectionsRepository
) : ViewModel() {

    // -------------------------
    // POINTS (Immutable List)
    // -------------------------
    private val _points = MutableStateFlow<List<LocationPoint>>(emptyList())
    val points: StateFlow<List<LocationPoint>> = _points.asStateFlow()
private  val Tag :String = "TrackingViewModel"
    // -------------------------
    // STATS (Speed, Distance, etc.)
    // -------------------------
    private val _stats = MutableStateFlow<LocationStats?>(null)
    val stats: StateFlow<LocationStats?> = _stats.asStateFlow()

    // -------------------------
    // REMAINING DISTANCE
    // -------------------------
    private val _remainingDistanceMeters = MutableStateFlow<Double?>(null)
    val remainingDistanceMeters: StateFlow<Double?> = _remainingDistanceMeters.asStateFlow()

    // -------------------------
    // AUTOCOMPLETE
    // -------------------------
    private val _originSuggestions = MutableStateFlow<List<PlaceSuggestionUi>>(emptyList())
    val originSuggestions = _originSuggestions.asStateFlow()

    private val _destSuggestions = MutableStateFlow<List<PlaceSuggestionUi>>(emptyList())
    val destSuggestions = _destSuggestions.asStateFlow()

    // -------------------------
    // SELECTED POSITIONS
    // -------------------------
    private val _originPoint = MutableStateFlow<LocationPoint?>(null)
    val originPoint = _originPoint.asStateFlow()

    private val _destPoint = MutableStateFlow<LocationPoint?>(null)
    val destPoint = _destPoint.asStateFlow()

    private val _routePoints = MutableStateFlow<List<LatLng>>(emptyList())
    val routePoints = _routePoints.asStateFlow()

    private val _routeDistanceMeters = MutableStateFlow<Int?>(null)
    val routeDistanceMeters = _routeDistanceMeters.asStateFlow()

    private val _routeDurationSeconds = MutableStateFlow<Int?>(null)
    val routeDurationSeconds = _routeDurationSeconds.asStateFlow()



    init {
        // Collect raw GPS locations → polyline + distance update
        viewModelScope.launch {
            observeLocationUseCase().collect { lp ->
                _points.value = _points.value + lp
                updateRemainingDistance(lp)
            }
        }

        // Collect stats stream → speed & total distance
        viewModelScope.launch {
            observeStats().collect { s ->
                _stats.value = s
            }
        }
    }

    fun loadRoute() {
        val o = originPoint.value ?: return
        val d = destPoint.value ?: return
        Log.d(Tag, "Origin CURRENT = ${originPoint.value}")
        Log.d(Tag, "Dest SELECTED  = ${destPoint.value}")

        viewModelScope.launch {
            val (points, distance, duration) =
                directionsRepository.getRoutePoints(o, d)

            _routePoints.value = points
            _routeDistanceMeters.value = distance
            _routeDurationSeconds.value = duration
        }
    }
    fun clearOrigin() {
        _originPoint.value = null
        _routePoints.value = emptyList()
    }

    fun clearDestination() {
        _destPoint.value = null
        _routePoints.value = emptyList()
    }


    // ----------------------------------------------------------
    // REMAINING DISTANCE CALCULATION
    // ----------------------------------------------------------
    fun updateRemainingDistance(current: LocationPoint?) {
        val dest = destPoint.value ?: return
        current ?: return

        val result = FloatArray(1)
        android.location.Location.distanceBetween(
            current.lat, current.lng,
            dest.lat, dest.lng,
            result
        )
        _remainingDistanceMeters.value = result[0].toDouble()
    }

    // ----------------------------------------------------------
    // PLACE AUTOCOMPLETE
    // ----------------------------------------------------------
    fun searchPlaces(q: String, isOrigin: Boolean) {
        if (q.isBlank()) {
            if (isOrigin) _originSuggestions.value = emptyList()
            else _destSuggestions.value = emptyList()
            return
        }

        viewModelScope.launch {
            try {
                val preds = placesRepo.getPlaceSuggestions(q)
                val uiList = preds.map { PlaceSuggestionUi(it.placeId, it.description) }
                if (isOrigin) _originSuggestions.value = uiList
                else _destSuggestions.value = uiList
            } catch (_: Exception) {
                if (isOrigin) _originSuggestions.value = emptyList()
                else _destSuggestions.value = emptyList()
            }
        }
    }

    // ----------------------------------------------------------
    // PLACE SELECTION
    // ----------------------------------------------------------
    fun onPlaceSelected(s: PlaceSuggestionUi, isOrigin: Boolean) {
        viewModelScope.launch {

            val coords = placesRepo.getPlaceCoordinates(s.placeId)

            if (isOrigin) {
                _originPoint.value = coords

                Log.d(Tag, "Origin SELECTED = ${originPoint.value}")
                Log.d(Tag, "Dest CURRENT     = ${destPoint.value}")

                // If destination is already chosen → load route now
                if (destPoint.value != null) {
                    loadRoute()
                }

            } else {
                _destPoint.value = coords

                Log.d(Tag, "Origin CURRENT = ${originPoint.value}")
                Log.d(Tag, "Dest SELECTED  = ${destPoint.value}")

                // Always load route after selecting destination
                loadRoute()
            }
        }
    }


    // ----------------------------------------------------------
    // USE CURRENT LOCATION AS ORIGIN
    // ----------------------------------------------------------
    fun setOriginFromCurrentLocation(context: Context) {
        viewModelScope.launch {
            val loc = getSafeCurrentLocation(context)
            loc ?: return@launch

            _originPoint.value = loc

            if (destPoint.value != null) {
                loadRoute()
            }
        }
    }


    @SuppressLint("MissingPermission")
    suspend fun getSafeCurrentLocation(context: Context): LocationPoint? {
        val fused = LocationServices.getFusedLocationProviderClient(context)

        // 1️⃣ Try fresh high-accuracy fix
        val fresh = fused.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            null
        ).await()

        if (fresh != null) {
            return LocationPoint(fresh.latitude, fresh.longitude, System.currentTimeMillis())
        }

        // 2️⃣ Try last known cached location
        val last = fused.lastLocation.await()
        if (last != null) {
            return LocationPoint(last.latitude, last.longitude, System.currentTimeMillis())
        }

        return null
    }


    // ----------------------------------------------------------
    // CLEAR ROUTE
    // ----------------------------------------------------------
    fun clearRoute() {
        _points.value = emptyList()
        _remainingDistanceMeters.value = null
    }
}
