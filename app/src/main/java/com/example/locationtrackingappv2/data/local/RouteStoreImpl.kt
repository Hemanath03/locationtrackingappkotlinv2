package com.example.locationtrackingappv2.data.local

import com.example.locationtrackingappv2.domain.repository.IRouteStore
import com.google.android.gms.maps.model.LatLng
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RouteStoreImpl @Inject constructor(): IRouteStore {
    @Volatile
    private var route: List<LatLng>? = null

    override fun setRoute(points: List<LatLng>) {
        route = points.toList()
    }

    override fun getRoute(): List<LatLng>? = route

    override fun clearRoute() { route = null }
}
