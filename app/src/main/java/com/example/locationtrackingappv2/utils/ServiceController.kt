package com.example.locationtrackingappv2.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.example.locationtrackingappv2.domain.repository.IRouteStore
import com.example.locationtrackingappv2.services.LocationService
import com.google.android.gms.maps.model.LatLng
import org.json.JSONArray
import org.json.JSONObject

object ServiceController {

    /**
     * Start LocationService for a planned trip
     * and send the route polyline (list of LatLng)
     */
    fun startTripService(context: Context, routePoints: List<LatLng>) {
        val intent = Intent(context, LocationService::class.java)

        // Convert List<LatLng> → JSON array string
        val json = JSONArray()
        routePoints.forEach {
            val obj = JSONObject()
            obj.put("latitude", it.latitude)
            obj.put("longitude", it.longitude)
            json.put(obj)
        }

        intent.putExtra("ROUTE_POINTS", json.toString())

        ContextCompat.startForegroundService(context, intent)
    }
    fun startTripService(context: Context, routePoints: List<LatLng>, routeStore: IRouteStore) {
        routeStore.setRoute(routePoints)
        val intent = Intent(context, LocationService::class.java)
        ContextCompat.startForegroundService(context, intent)
    }


    /**
     * Start service WITHOUT route (old behaviour)
     */
    fun startService(context: Context) {
        val intent = Intent(context, LocationService::class.java)
        ContextCompat.startForegroundService(context, intent)
    }

    /**
     * Stop location tracking service
     */
    fun stopService(context: Context) {
        val intent = Intent(context, LocationService::class.java)
        context.stopService(intent)
    }
}
