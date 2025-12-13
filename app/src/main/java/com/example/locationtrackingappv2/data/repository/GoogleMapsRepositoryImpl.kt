package com.example.locationtrackingappv2.data.repository

import android.util.Log
import com.example.locationtrackingappv2.domain.entity.LocationPoint
import com.example.locationtrackingappv2.domain.entity.PlaceSuggestion
import com.example.locationtrackingappv2.domain.repository.GoogleMapsRepository
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleMapsRepositoryImpl @Inject constructor(
    private val placesClient: PlacesClient
) : GoogleMapsRepository {

    // 🟦 Each autocomplete (typing → suggestion selection) must use ONE token
    private var sessionToken: AutocompleteSessionToken? = null
    private var tag: String = "GoogleMapsRepositoryImpl"

    private fun getOrCreateSessionToken(): AutocompleteSessionToken {
        if (sessionToken == null) {
            sessionToken = AutocompleteSessionToken.newInstance()
        }
        return sessionToken!!
    }

    override suspend fun getPlaceSuggestions(query: String): List<PlaceSuggestion> {
        return try {
            val token = getOrCreateSessionToken()

            val request = FindAutocompletePredictionsRequest.builder()
                .setSessionToken(token)
                .setQuery(query)
                .build()

            val response = placesClient.findAutocompletePredictions(request).await()
            Log.d( tag, "getPlaceSuggestions: $response")
            response.autocompletePredictions.map {
                PlaceSuggestion(
                    placeId = it.placeId,
                    description = it.getFullText(null).toString()
                )
            }

        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun getPlaceCoordinates(placeId: String): LocationPoint {
        return try {

            val request = FetchPlaceRequest.builder(
                placeId,
                listOf(Place.Field.LAT_LNG)
            ).build()

            val response = placesClient.fetchPlace(request).await()

            val latLng = response.place.latLng
                ?: throw IllegalStateException("LatLng missing from place")

            // 🟦 Once a place is selected, END session → avoid duplicated billing
            sessionToken = null
            Log.d( tag, "getPlaceCoordinates: $response")
            LocationPoint(
                latLng.latitude,
                latLng.longitude,
                System.currentTimeMillis()
            )

        } catch (e: Exception) {
            e.printStackTrace()
            LocationPoint(0.0, 0.0, System.currentTimeMillis())
        }
    }
}
