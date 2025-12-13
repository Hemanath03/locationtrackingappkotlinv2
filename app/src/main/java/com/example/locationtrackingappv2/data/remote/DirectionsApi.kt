package com.example.locationtrackingappv2.data.remote

import com.example.locationtrackingappv2.data.remote.dto.DirectionsRequest
import com.example.locationtrackingappv2.data.remote.dto.DirectionsResponse
import com.example.locationtrackingappv2.BuildConfig
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface DirectionsApi {

    @POST("directions/v2:computeRoutes")
    suspend fun getRoute(
        @Body body: DirectionsRequest,
        @Header("X-Goog-Api-Key") apiKey: String = BuildConfig.MAPS_API_KEY,
        @Header("X-Goog-FieldMask")
        masks: String = "routes.distanceMeters,routes.duration,routes.polyline.encodedPolyline"
    ): DirectionsResponse

}

