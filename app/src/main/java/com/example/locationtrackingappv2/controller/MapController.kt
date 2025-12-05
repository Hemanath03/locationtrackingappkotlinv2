package com.example.locationtrackingappv2.controller

import androidx.appcompat.app.AppCompatActivity
import com.example.locationtrackingappv2.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions


class MapController(
    activity: AppCompatActivity
) : OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private var polyline: Polyline? = null

    init {
        val fragment = activity.supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        fragment.getMapAsync(this)
    }

    override fun onMapReady(g: GoogleMap) {
        map = g
        map.uiSettings.isZoomControlsEnabled = true
    }

    fun drawRoute(points: List<LatLng>) {
        polyline?.remove()
        polyline = map.addPolyline(PolylineOptions().addAll(points).width(10f))

        if (points.isNotEmpty()) {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(points.first(), 14f))
        }
    }
}
