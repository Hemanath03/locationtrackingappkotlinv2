package com.example.locationtrackingappv2.data.remote

import com.example.locationtrackingappv2.data.remote.dto.SnapToRoadResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface RoadsApi {

    @GET("v1/snapToRoads")
    suspend fun snapToRoad(
        @Query("locations") latLng: String,
        @Query("key") apiKey: String
    ): SnapToRoadResponse
}
