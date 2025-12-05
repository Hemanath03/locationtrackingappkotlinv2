package com.example.locationtrackingappv2.data.repository

import com.example.locationtrackingappv2.data.remote.GoogleMapsRemoteDataSource
import com.example.locationtrackingappv2.data.dto.*
import com.example.locationtrackingappv2.domain.models.*
import com.example.locationtrackingappv2.domain.repository.GoogleMapsRepository
import com.google.gson.Gson
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleMapsRepositoryImpl @Inject constructor(
    private val remote: GoogleMapsRemoteDataSource,
    private val gson: Gson
) : GoogleMapsRepository {

    /* -------------------------------------------------------------------------- */
    /*   AUTOCOMPLETE                                                            */
    /* -------------------------------------------------------------------------- */
    override suspend fun getPlaceSuggestions(
        query: String,
        sessionToken: String
    ): List<Prediction> {

        if (query.isBlank()) throw IllegalArgumentException("Input cannot be empty")

        val requestBody = mapOf(
            "input" to query,
            "includedRegionCodes" to listOf("au"),
            "languageCode" to "en",
            "sessionToken" to sessionToken
        )

        val json = remote.fetchAutocomplete(requestBody)

        val dto = gson.fromJson(json, AutocompleteResponseDto::class.java)

        if (dto.status != null && dto.status != "OK") {
            throw Exception("Autocomplete failed: ${dto.status}")
        }

        return dto.toDomainList()
    }

    /* -------------------------------------------------------------------------- */
    /*   PLACE DETAILS                                                            */
    /* -------------------------------------------------------------------------- */
    override suspend fun getPlaceCoordinates(
        placeId: String,
        sessionToken: String
    ): Prediction? {

        if (placeId.isBlank()) throw IllegalArgumentException("Place ID cannot be empty")

        val json = remote.fetchPlaceDetails(placeId, sessionToken)

        val dto = gson.fromJson(json, PlaceDetailsDto::class.java)

        if (dto.status != null && dto.status != "OK") {
            throw Exception("Place details failed: ${dto.status}")
        }

        return dto.toPredictionWithLatLng()
    }

    /* -------------------------------------------------------------------------- */
    /*   ROUTES                                                                   */
    /* -------------------------------------------------------------------------- */
    override suspend fun computeRoutes(
        origin: PlaceLatLng,
        destination: PlaceLatLng
    ): RouteInfo {

        val body = mapOf(
            "origin" to mapOf(
                "location" to mapOf(
                    "latLng" to mapOf(
                        "latitude" to origin.lat,
                        "longitude" to origin.lng
                    )
                )
            ),
            "destination" to mapOf(
                "location" to mapOf(
                    "latLng" to mapOf(
                        "latitude" to destination.lat,
                        "longitude" to destination.lng
                    )
                )
            ),
            "travelMode" to "DRIVE",
            "routingPreference" to "TRAFFIC_AWARE",
            "computeAlternativeRoutes" to false,
            "languageCode" to "en",
            "units" to "METRIC"
        )

        val json = remote.computeRoutes(body)

        val dto = gson.fromJson(json, RoutesResponseDto::class.java)

        if (dto.status != null && dto.status != "OK") {
            throw Exception("Routes API failed: ${dto.status} - ${dto.errorMessage}")
        }

        return dto.toRouteInfo()
            ?: throw Exception("Failed to parse route info")
    }
}
