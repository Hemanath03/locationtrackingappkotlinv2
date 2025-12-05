package com.example.locationtrackingappv2.data.dto

import com.google.gson.annotations.SerializedName

/**
 * Minimal Routes v2 DTO:
 * We only parse the top-level useful bits: routes[].distanceMeters, routes[].duration, routes[].polyline.encodedPolyline
 *
 * Real response contains many fields — we keep only the ones required to compute/draw route.
 */
data class RoutesResponseDto(
    @SerializedName("routes")
    val routes: List<RouteDto> = emptyList(),

    @SerializedName("status")
    val status: String? = null,

    @SerializedName("error_message")
    val errorMessage: String? = null
)

data class RouteDto(
    @SerializedName("distanceMeters")
    val distanceMeters: Int? = null,

    /**
     * Routes v2 sometimes uses duration/durationSeconds; include both possibilities.
     */
    @SerializedName("duration")
    val duration: String? = null,

    @SerializedName("durationSeconds")
    val durationSeconds: Int? = null,

    @SerializedName("polyline")
    val polyline: PolylineDto? = null
)

data class PolylineDto(
    @SerializedName("encodedPolyline")
    val encodedPolyline: String? = null
)
