package com.example.locationtrackingappv2.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.locationtrackingappv2.domain.entity.LocationPoint
import com.example.locationtrackingappv2.domain.entity.LocationStats
import com.example.locationtrackingappv2.domain.repository.GoogleMapsRepository
import com.example.locationtrackingappv2.domain.usecase.ObserveLocationStatsUseCase
import com.example.locationtrackingappv2.domain.usecase.ObserveLocationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlaceSuggestionUi(
    val placeId: String,
    val description: String
)

@HiltViewModel
class TrackingViewModel @Inject constructor(
    observeLocationUseCase: ObserveLocationUseCase,
    private val observeStats: ObserveLocationStatsUseCase,
    private val placesRepo: GoogleMapsRepository
) : ViewModel() {

    // POINTS STREAM — list optimized
    private val _points = MutableStateFlow(mutableListOf<LocationPoint>())
    val points: StateFlow<List<LocationPoint>> = _points.map { it.toList() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // STATS STREAM
    val stats: StateFlow<LocationStats?> =
        observeStats().stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // AUTOCOMPLETE STREAMS
    private val _originSuggestions = MutableStateFlow<List<PlaceSuggestionUi>>(emptyList())
    val originSuggestions = _originSuggestions.asStateFlow()

    private val _destSuggestions = MutableStateFlow<List<PlaceSuggestionUi>>(emptyList())
    val destSuggestions = _destSuggestions.asStateFlow()

    // SELECTED PLACE POINTS
    private val _originPoint = MutableStateFlow<LocationPoint?>(null)
    val originPoint = _originPoint.asStateFlow()

    private val _destPoint = MutableStateFlow<LocationPoint?>(null)
    val destPoint = _destPoint.asStateFlow()

    init {
        // Collect raw locations → polyline
        viewModelScope.launch {
            observeLocationUseCase().collect { lp ->
                _points.value.add(lp)
            }
        }
    }

    // AUTOCOMPLETE HANDLING
    fun searchPlaces(q: String, isOrigin: Boolean) {
        if (q.isBlank()) {
            if (isOrigin) _originSuggestions.value = emptyList()
            else _destSuggestions.value = emptyList()
            return
        }

        viewModelScope.launch {
            try {
                val preds = placesRepo.getPlaceSuggestions(q)
                val uiList = preds.map {
                    PlaceSuggestionUi(it.placeId, it.description)
                }
                if (isOrigin) _originSuggestions.value = uiList
                else _destSuggestions.value = uiList
            } catch (e: Exception) {
                _originSuggestions.value = emptyList()
                _destSuggestions.value = emptyList()
            }
        }
    }


    fun onPlaceSelected(suggestion: PlaceSuggestionUi, isOrigin: Boolean) {
        viewModelScope.launch {
            val coords = placesRepo.getPlaceCoordinates(suggestion.placeId)
            if (isOrigin) _originPoint.value = coords else _destPoint.value = coords
        }
    }

    fun clearRoute() {
        _points.value = mutableListOf()
    }
}
