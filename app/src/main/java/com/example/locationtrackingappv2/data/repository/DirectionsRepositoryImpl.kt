package com.example.locationtrackingappv2.data.repository

import android.util.Log
import com.example.locationtrackingappv2.data.remote.DirectionsApi
import com.example.locationtrackingappv2.data.remote.dto.DirectionsRequest
import com.example.locationtrackingappv2.data.remote.dto.LatLngLiteral
import com.example.locationtrackingappv2.data.remote.dto.LatLngWrapper
import com.example.locationtrackingappv2.data.remote.dto.RouteLocation
import com.example.locationtrackingappv2.domain.entity.LocationPoint
import com.example.locationtrackingappv2.domain.repository.DirectionsRepository
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import retrofit2.HttpException
import javax.inject.Inject

class DirectionsRepositoryImpl @Inject constructor(
    private val api: DirectionsApi
) : DirectionsRepository {

    override suspend fun getRoutePoints(
        origin: LocationPoint,
        dest: LocationPoint
    ): Triple<List<LatLng>, Int, Int> {

        Log.d("DirectionsRepo", "➡ Requesting Routes API v2 route...")

        // Build request body
        val request = DirectionsRequest(
            origin = RouteLocation(
                location = LatLngWrapper(
                    latLng = LatLngLiteral(origin.lat, origin.lng)
                )
            ),
            destination = RouteLocation(
                location = LatLngWrapper(
                    latLng = LatLngLiteral(dest.lat, dest.lng)
                )
            ),
            travelMode = "DRIVE",
            polylineQuality = "HIGH_QUALITY",
            polylineEncoding = "ENCODED_POLYLINE"
        )

        return try {
            val response = api.getRoute(request)

            if (response.routes.isNullOrEmpty()) {
                Log.e("DirectionsRepo", "❌ No routes found from API")
                return Triple(emptyList(), 0, 0)
            }

            val route = response.routes.first()

            val encoded = route.polyline?.encodedPolyline
            if (encoded.isNullOrEmpty()) {
                Log.e("DirectionsRepo", "❌ No encoded polyline in route")
                return Triple(emptyList(), 0, 0)
            }

            val distanceMeters = route.distanceMeters ?: 0
            val durationSeconds = parseDuration(route.duration)

            Log.d("DirectionsRepo", "✔ distance = ${distanceMeters}m, duration = ${durationSeconds}s")

            val decodedPoints = try {
                PolyUtil.decode(encoded)
            } catch (e: Exception) {
                Log.e("DirectionsRepo", "❌ Polyline decode failed: ${e.message}")
                emptyList()
            }

            Triple(decodedPoints, distanceMeters, durationSeconds)

        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Log.e("DirectionsRepo", "❌ HTTP ${e.code()} BODY = $errorBody")
            return Triple(emptyList(), 0, 0)
        }
        catch (e: Exception) {
            Log.e("DirectionsRepo", "❌ Routes API FAILED: ${e.message}")
            e.printStackTrace()
            Triple(emptyList(), 0, 0)
        }
    }


    private fun parseDuration(text: String?): Int {
        if (text.isNullOrEmpty()) return 0
        return text.removeSuffix("s").toIntOrNull() ?: 0
    }
}
