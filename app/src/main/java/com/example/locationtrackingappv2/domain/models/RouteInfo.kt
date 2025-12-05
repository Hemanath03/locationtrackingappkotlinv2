package com.example.locationtrackingappv2.domain.models

data class RouteInfo(
    val distanceMeters: Int,
    val durationSeconds: Int,
    val polylinePoints: List<com.google.android.gms.maps.model.LatLng>
)
