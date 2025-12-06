package com.example.locationtrackingappv2.domain.entity

/**
 * LocationStats represents the latest computed stats from location stream.
 *
 * - last: last reported point
 * - totalDistanceMeters: cumulative distance measured since tracking started
 * - instantSpeedMps: instantaneous speed between last two points (m/s)
 * - averageSpeedMps: totalDistance / totalTime (m/s), 0 if not computable
 */
data class LocationStats(
    val last: LocationPoint,
    val totalDistanceMeters: Double,
    val instantSpeedMps: Double,
    val averageSpeedMps: Double
)
