package com.example.locationtrackingappv2.domain.repository

import android.content.Context
import com.example.locationtrackingappv2.domain.models.TollMatch
import com.google.android.gms.maps.model.LatLng

interface ITollRepository {
    suspend fun loadTolls(context: Context)
    fun isPointOnToll(lat: Double, lng: Double): Boolean
    fun getTollsIntersecting(routePoints: List<LatLng>): List<TollMatch>
}
