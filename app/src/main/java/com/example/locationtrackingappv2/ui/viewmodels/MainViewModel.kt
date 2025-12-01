package com.example.locationtrackingappv2.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.locationtrackingappv2.data.LocationRepository
import com.example.locationtrackingappv2.models.LocationData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repo: LocationRepository
) : ViewModel() {

    private val _locationState = MutableStateFlow<LocationData?>(null)
    val locationState: StateFlow<LocationData?> =  repo.locationFlow

    init {
        viewModelScope.launch {
            repo.locationFlow.collect {
                _locationState.value = it
            }
        }
    }
}