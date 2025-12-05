package com.example.locationtrackingappv2.domain.repository

import com.google.android.gms.maps.model.LatLng

interface IRouteStore {
    fun setRoute(points: List<LatLng>)
    fun getRoute(): List<LatLng>?
    fun clearRoute()
}
