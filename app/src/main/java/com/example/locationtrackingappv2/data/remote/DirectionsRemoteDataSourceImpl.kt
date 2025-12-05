//package com.example.locationtrackingappv2.data.remote
//
//import retrofit2.http.GET
//import retrofit2.http.Query
//
//interface DirectionsApi {
//    @GET("directions/json")
//    suspend fun getDirections(
//        @Query("origin") origin: String,
//        @Query("destination") dest: String,
//        @Query("key") key: String,
//        @Query("mode") mode: String = "driving"
//    ): Response<DirectionsResponseDto>
//}
