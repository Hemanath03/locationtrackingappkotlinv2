package com.example.locationtrackingappv2.data.remote.dto

data class DirectionsResponse(
    val routes: List<RoutesItem>?
)

data class RoutesItem(
    val distanceMeters: Int?,
    val duration: String?,
    val durationInTraffic: String?,
    val polyline: Polyline?
)

data class Polyline(
    val encodedPolyline: String?
)

