package com.example.locationtrackingappv2.data.repository

import com.example.locationtrackingappv2.domain.entity.LocationPoint
import com.example.locationtrackingappv2.domain.entity.PlaceSuggestion
import com.example.locationtrackingappv2.domain.repository.GoogleMapsRepository
import com.google.android.gms.common.api.ApiException
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

    override suspend fun getPlaceSuggestions(query: String): List<PlaceSuggestion> {
        return try {
            val request = FindAutocompletePredictionsRequest.builder()
                .setQuery(query)
                .build()

            val response = placesClient.findAutocompletePredictions(request).await()

            response.autocompletePredictions.map {
                PlaceSuggestion(
                    placeId = it.placeId,
                    description = it.getFullText(null).toString()
                )
            }

        } catch (e: ApiException) {
            // API errors: bad key, billing disabled, quota exceeded, etc.
            e.printStackTrace()
            emptyList()

        } catch (e: Exception) {
            // Network or unknown errors
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
                ?: throw IllegalStateException("LatLng not available")

            LocationPoint(
                latLng.latitude,
                latLng.longitude,
                System.currentTimeMillis()
            )

        } catch (e: ApiException) {
            e.printStackTrace()
            // return safe fallback so ViewModel does NOT crash UI
            LocationPoint(0.0, 0.0, System.currentTimeMillis())
        } catch (e: Exception) {
            e.printStackTrace()
            LocationPoint(0.0, 0.0, System.currentTimeMillis())
        }
    }


}
