package com.example.locationtrackingappv2.data.remote.dto

data class SnapToRoadResponse(
    val snappedPoints: List<SnappedPoint>?
)

data class SnappedPoint(
    val location: SnappedLocation
)

data class SnappedLocation(
    val latitude: Double,
    val longitude: Double
)
