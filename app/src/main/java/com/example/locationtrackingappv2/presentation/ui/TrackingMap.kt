package com.example.locationtrackingappv2.presentation.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import com.example.locationtrackingappv2.domain.entity.LocationPoint
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.tasks.await

// Maps Compose 2.14.0
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState

@SuppressLint("MissingPermission")
@Composable
fun TrackingMap(
    points: List<LocationPoint>,
    origin: LocationPoint?,
    destination: LocationPoint?,
    route: List<LatLng>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    val camera = rememberCameraPositionState()
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }

    // 1️⃣ Auto zoom to route
    LaunchedEffect(route) {
        if (route.isNotEmpty()) {
            val builder = com.google.android.gms.maps.model.LatLngBounds.Builder()
            route.forEach { builder.include(it) }
            val bounds = builder.build()

            camera.animate(
                CameraUpdateFactory.newLatLngBounds(bounds, 100)
            )
        }
    }

    // 2️⃣ Initial camera = current device location
    LaunchedEffect(true) {
        val loc = fusedClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            null
        ).await()

        loc?.let {
            val pos = LatLng(it.latitude, it.longitude)
            currentLocation = pos

            pos.let { safe ->
                camera.move(CameraUpdateFactory.newLatLngZoom(safe, 17f))
            }
        }
    }


    Box(modifier = modifier.fillMaxSize()) {

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = camera,
            properties = MapProperties(
                mapType = MapType.HYBRID)
        ) {

            // ⭐ Draw route
            if (route.isNotEmpty()) {
                Polyline(points = route, color = Color.Blue, width = 10f)
            }

            // ⭐ Current location marker
            currentLocation?.let {
                Marker(
                    MarkerState(it),
                    title = "You",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                )
            }

            // ⭐ Origin marker
            origin?.let {
                Marker(MarkerState(LatLng(it.lat, it.lng)), title = "Origin")
            }

            // ⭐ Destination marker
            destination?.let {
                Marker(MarkerState(LatLng(it.lat, it.lng)), title = "Destination")
            }
        }
    }
}
