package com.example.locationtrackingappv2.domain.models

data class Prediction(
    val placeId: String,
    val description: String,
    var lat: Double? = null,
    var lng: Double? = null
)
