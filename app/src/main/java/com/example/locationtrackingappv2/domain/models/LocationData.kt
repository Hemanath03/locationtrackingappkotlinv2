package com.example.locationtrackingappv2.domain.models

data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val speed: Float,
    val isTollRoad: Boolean = false
)
