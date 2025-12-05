package com.example.locationtrackingappv2.controller

import android.content.Context
import com.example.locationtrackingappv2.utils.ServiceController
import com.google.android.gms.maps.model.LatLng

class TripNavigator(private val context: Context) {

    fun startTrip(points: List<LatLng>) {
        ServiceController.startTripService(context, points)
    }

    fun stopTrip() {
        ServiceController.stopService(context)
    }
}
