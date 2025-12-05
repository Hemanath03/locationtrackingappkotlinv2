package com.example.locationtrackingappv2.data.dto

import com.example.locationtrackingappv2.domain.models.PlaceLatLng
import com.example.locationtrackingappv2.domain.models.RouteInfo
import com.example.locationtrackingappv2.domain.models.Prediction
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil

// Autocomplete DTO -> domain Prediction
fun PredictionDto.toDomain(): Prediction {
    // Prefer structuredFormatting.mainText + secondaryText for display if available
    val desc = structuredFormatting?.let { sf ->
        val main = sf.mainText ?: ""
        val secondary = sf.secondaryText ?: ""
        if (secondary.isNotBlank()) "$main, $secondary" else main
    } ?: (description ?: "")

    return Prediction(
        placeId = placeId ?: "",
        description = desc
    )
}

fun AutocompleteResponseDto.toDomainList(): List<Prediction> =
    predictions.mapNotNull { dto ->
        if (dto.placeId.isNullOrBlank()) null else dto.toDomain()
    }

// Place details -> Prediction with lat/lng (nullable)
fun PlaceDetailsDto.toPredictionWithLatLng(): Prediction? {
    val result = this.result ?: return null
    val loc = result.geometry?.location ?: return null
    val id = result.placeId ?: return null
    val desc = result.formattedAddress ?: ""
    return Prediction(
        placeId = id,
        description = desc,
        lat = loc.lat,
        lng = loc.lng
    )
}

fun PlaceDetailsDto.toPlaceLatLng(): PlaceLatLng? {
    val loc = this.result?.geometry?.location ?: return null
    val lat = loc.lat ?: return null
    val lng = loc.lng ?: return null
    return PlaceLatLng(lat, lng)
}

// Routes DTO -> RouteInfo
fun RoutesResponseDto.toRouteInfo(): RouteInfo? {
    if (routes.isEmpty()) return null
    val r = routes.first()
    val encoded = r.polyline?.encodedPolyline
    if (encoded.isNullOrBlank()) return null

    val decoded: List<LatLng> = try {
        PolyUtil.decode(encoded)
    } catch (e: Exception) {
        emptyList()
    }

    val durationSec = r.durationSeconds ?: 0
    val distance = r.distanceMeters ?: 0

    return RouteInfo(
        distanceMeters = distance,
        durationSeconds = durationSec,
        polylinePoints = decoded
    )
}
