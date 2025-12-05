package com.example.locationtrackingappv2.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.locationtrackingappv2.domain.models.*
import com.example.locationtrackingappv2.domain.usecase.ComputeRoutesUseCase
import com.example.locationtrackingappv2.domain.usecase.GetPlaceLatLngUseCase
import com.example.locationtrackingappv2.domain.usecase.GetPredictionsUseCase
import com.example.locationtrackingappv2.domain.usecase.GetTollsOnRouteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class TripViewModel @Inject constructor(
    private val getPredictionsUseCase: GetPredictionsUseCase,
    private val getPlaceLatLngUseCase: GetPlaceLatLngUseCase,
    private val computeRoutesUseCase: ComputeRoutesUseCase,
    private val getTollsOnRouteUseCase: GetTollsOnRouteUseCase
) : ViewModel() {

    // -----------------------------
    // STATE
    // -----------------------------
    private val _predictions = MutableStateFlow<List<Prediction>>(emptyList())
    val predictions: StateFlow<List<Prediction>> = _predictions.asStateFlow()

    private val _route = MutableStateFlow<RouteInfo?>(null)
    val route = _route.asStateFlow()

    private val _tolls = MutableStateFlow<List<TollMatch>>(emptyList())
    val tolls = _tolls.asStateFlow()

    private val _fromPlace = MutableStateFlow<Prediction?>(null)
    val fromPlace = _fromPlace.asStateFlow()

    private val _toPlace = MutableStateFlow<Prediction?>(null)
    val toPlace = _toPlace.asStateFlow()

    private var originLatLng: PlaceLatLng? = null
    private var destLatLng: PlaceLatLng? = null

    // UI event channel
    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    // Autocomplete flows
    private val fromQuery = MutableSharedFlow<String>(replay = 1)
    private val toQuery = MutableSharedFlow<String>(replay = 1)

    private var fromJob: Job? = null
    private var toJob: Job? = null

    // Session token (Google best practice)
    private var sessionToken = UUID.randomUUID().toString()

    init {
        startListeningForAutocomplete()
    }

    // -----------------------------------------------------------
    // AUTOCOMPLETE (Debounced)
    // -----------------------------------------------------------
    private fun startListeningForAutocomplete() {

        fromJob?.cancel()
        fromJob = viewModelScope.launch {
            fromQuery
                .debounce(300.milliseconds)
                .filter { it.isNotBlank() }
                .collectLatest { q -> fetchPredictions(q) }
        }

        toJob?.cancel()
        toJob = viewModelScope.launch {
            toQuery
                .debounce(300.milliseconds)
                .filter { it.isNotBlank() }
                .collectLatest { q -> fetchPredictions(q) }
        }
    }

    fun onFromQuery(text: String) {
        viewModelScope.launch { fromQuery.emit(text) }
    }

    fun onToQuery(text: String) {
        viewModelScope.launch { toQuery.emit(text) }
    }

    private suspend fun fetchPredictions(query: String) {
        try {
            val results = getPredictionsUseCase(query, sessionToken)
            _predictions.value = results
        } catch (e: Exception) {
            _predictions.value = emptyList()
            _events.send(UiEvent.Error("Prediction failed: ${e.message}"))
        }
    }

    // -----------------------------------------------------------
    // PLACE SELECTION (Fetch lat/lng)
    // -----------------------------------------------------------
    fun selectPrediction(prediction: Prediction, isFrom: Boolean) {
        viewModelScope.launch {
            try {
                val result = getPlaceLatLngUseCase(prediction.placeId, sessionToken)
                if (result != null && result.lat != null && result.lng != null) {

                    val latLng = PlaceLatLng(result.lat!!, result.lng!!)

                    if (isFrom) {
                        originLatLng = latLng
                        _fromPlace.value = prediction.copy(lat = result.lat, lng = result.lng)
                    } else {
                        destLatLng = latLng
                        _toPlace.value = prediction.copy(lat = result.lat, lng = result.lng)
                    }
                } else {
                    _events.send(UiEvent.Error("Unable to resolve place details"))
                }
            } catch (e: Exception) {
                _events.send(UiEvent.Error("Place details error: ${e.message}"))
            }
        }
    }

    // -----------------------------------------------------------
    // ROUTE COMPUTATION
    // -----------------------------------------------------------
    fun computeRoute() {
        viewModelScope.launch {

            val origin = originLatLng
            val dest = destLatLng

            if (origin == null || dest == null) {
                _events.send(UiEvent.Error("Select both From and To"))
                return@launch
            }

            _events.send(UiEvent.Info("Computing route..."))

            try {
                val result = computeRoutesUseCase(origin, dest)
                _route.value = result

                val tollMatches = getTollsOnRouteUseCase(result.polylinePoints)
                _tolls.value = tollMatches

                _events.send(UiEvent.Info("Route ready (${tollMatches.size} tolls detected)"))

            } catch (e: Exception) {
                _events.send(UiEvent.Error("Route failed: ${e.message}"))
            }
        }
    }

    // -----------------------------------------------------------
    // EVENTS
    // -----------------------------------------------------------
    sealed class UiEvent {
        data class Error(val message: String) : UiEvent()
        data class Info(val message: String) : UiEvent()
    }
}
