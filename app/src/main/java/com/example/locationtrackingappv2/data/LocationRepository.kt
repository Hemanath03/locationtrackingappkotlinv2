package com.example.locationtrackingappv2.data

import com.example.locationtrackingappv2.models.LocationData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepository @Inject constructor() {

    private val _locationFlow = MutableStateFlow<LocationData?>(null)
    val locationFlow = _locationFlow.asStateFlow()

    fun updateLocation(lat: Double, lng: Double, speed: Float, isToll: Boolean) {
        _locationFlow.value = LocationData(lat, lng, speed, isToll)
    }

}

