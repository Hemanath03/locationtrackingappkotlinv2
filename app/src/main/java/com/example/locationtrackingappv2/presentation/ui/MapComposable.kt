package com.example.locationtrackingappv2.presentation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.LatLng
import com.example.locationtrackingappv2.domain.entity.LocationPoint
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.android.gms.maps.CameraUpdateFactory

/**
 * MapComposable shows a GoogleMap with:
 * - polyline from points
 * - markers for current, origin, destination (optional)
 */
@Composable
fun TrackingMap(
    points: List<LocationPoint>,
    origin: LocationPoint?,
    destination: LocationPoint?,
    modifier: Modifier = Modifier
) {
    val cameraPositionState = rememberCameraPositionState()
    val latLngPoints = points.map { LatLng(it.lat, it.lng) }

    LaunchedEffect(latLngPoints.lastOrNull()) {
        // Move camera to last point if available
        latLngPoints.lastOrNull()?.let {
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(it, 16f)
            )
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
        ) {
            if (latLngPoints.isNotEmpty()) {
                Polyline(points = latLngPoints)
                // current location marker
                Marker(
                    state = MarkerState(position = latLngPoints.last()),
                    title = "You"
                )
            }
            origin?.let {
                Marker(state = MarkerState(position = LatLng(it.lat, it.lng)), title = "Origin")
            }
            destination?.let {
                Marker(state = MarkerState(position = LatLng(it.lat, it.lng)), title = "Destination")
            }
        }
    }
}
