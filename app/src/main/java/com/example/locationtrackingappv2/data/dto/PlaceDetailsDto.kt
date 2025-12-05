package com.example.locationtrackingappv2.data.dto

import com.google.gson.annotations.SerializedName

/**
 * Minimal DTO for Place Details response:
 * expected shape:
 * {
 *   "result": {
 *     "place_id": "...",
 *     "formatted_address": "...",
 *     "geometry": {
 *       "location": { "lat": -33.86, "lng": 151.20 }
 *     }
 *   },
 *   "status": "OK"
 * }
 */
data class PlaceDetailsDto(
    @SerializedName("result")
    val result: PlaceResultDto? = null,

    @SerializedName("status")
    val status: String? = null
)

data class PlaceResultDto(
    @SerializedName("place_id")
    val placeId: String? = null,

    @SerializedName("formatted_address")
    val formattedAddress: String? = null,

    @SerializedName("geometry")
    val geometry: GeometryDto? = null
)

data class GeometryDto(
    @SerializedName("location")
    val location: LatLngDto? = null
)

data class LatLngDto(
    @SerializedName("lat")
    val lat: Double? = null,

    @SerializedName("lng")
    val lng: Double? = null
)
