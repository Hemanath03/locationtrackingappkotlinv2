package com.example.locationtrackingappv2.data.remote.dto


data class DirectionsRequest(
    val origin: RouteLocation,
    val destination: RouteLocation,
    val travelMode: String = "DRIVE",
    val polylineQuality: String = "HIGH_QUALITY",
    val polylineEncoding: String = "ENCODED_POLYLINE"
)

data class RouteLocation(
    val location: LatLngWrapper
)

data class LatLngWrapper(
    val latLng: LatLngLiteral
)

data class LatLngLiteral(
    val latitude: Double,
    val longitude: Double
)
