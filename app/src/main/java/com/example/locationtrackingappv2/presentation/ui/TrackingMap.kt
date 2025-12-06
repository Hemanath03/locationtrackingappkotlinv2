package com.example.locationtrackingappv2.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.maps.android.compose.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.example.locationtrackingappv2.domain.entity.LocationPoint

@Composable
fun TrackingMap(
    points: List<LocationPoint>,
    origin: LocationPoint?,
    destination: LocationPoint?,
    followUser: Boolean,
    onToggleFollow: () -> Unit,
    modifier: Modifier = Modifier
) {
    val camera = rememberCameraPositionState()
    val latLngPoints = points.map { LatLng(it.lat, it.lng) }

    LaunchedEffect(latLngPoints.lastOrNull(), followUser) {
        if (followUser) {
            latLngPoints.lastOrNull()?.let {
                camera.animate(CameraUpdateFactory.newLatLngZoom(it, 17f))
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = camera
        ) {
            if (latLngPoints.isNotEmpty()) {
                Polyline(points = latLngPoints, color = Color(0xFF0088FF))
                Marker(state = MarkerState(latLngPoints.last()), title = "You")
            }
            origin?.let { Marker(state = MarkerState(LatLng(it.lat, it.lng)), title = "Origin") }
            destination?.let { Marker(state = MarkerState(LatLng(it.lat, it.lng)), title = "Destination") }
        }

        // FOLLOW ME BUTTON
        Box(modifier = Modifier
            .padding(12.dp)
            .align(androidx.compose.ui.Alignment.BottomEnd)) {

            Button(onClick = onToggleFollow) {
                Text(if (followUser) "Following" else "Follow Me")
            }
        }
    }
}
