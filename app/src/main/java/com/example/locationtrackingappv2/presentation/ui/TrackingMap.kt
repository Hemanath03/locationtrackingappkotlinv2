package com.example.locationtrackingappv2.presentation.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.locationtrackingappv2.domain.entity.LocationPoint
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.example.locationtrackingappv2.R
import com.example.locationtrackingappv2.utils.vectorToBitmap

@SuppressLint("MissingPermission")
@Composable
fun TrackingMap(
    points: List<LocationPoint>,
    destination: LocationPoint?,
    fullRoute: List<LatLng>,
    remainingRoute: List<LatLng>,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val fused = LocationServices.getFusedLocationProviderClient(context)

    val camera = rememberCameraPositionState()
    val coroutineScope = rememberCoroutineScope()

    var lastDriverLocation by remember { mutableStateOf<LatLng?>(null) }

    // 1) Move camera on first load
    LaunchedEffect(true) {
        val loc = fused.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
        loc?.let {
            val pos = LatLng(it.latitude, it.longitude)
            lastDriverLocation = pos
            camera.move(CameraUpdateFactory.newLatLngZoom(pos, 17f))
        }
    }

    // 2) Auto zoom to full route once loaded
    LaunchedEffect(fullRoute) {
        if (fullRoute.isNotEmpty()) {
            val builder = com.google.android.gms.maps.model.LatLngBounds.Builder()
            fullRoute.forEach { builder.include(it) }
            camera.animate(CameraUpdateFactory.newLatLngBounds(builder.build(), 100))
        }
    }

    Box(modifier = modifier.fillMaxSize()) {

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = camera,
            properties = MapProperties(
                mapType = MapType.NORMAL
            )
        ) {

            // FULL ROUTE (Gray)
            if (fullRoute.isNotEmpty()) {
                Polyline(
                    points = fullRoute,
                    color = Color.LightGray,
                    width = 8f
                )
            }

            // REMAINING ROUTE (Blue)
            if (remainingRoute.isNotEmpty()) {
                Polyline(
                    points = remainingRoute,
                    color = Color.Blue,
                    width = 10f
                )
            }

            // DRIVER MARKER (Smooth animated)
            val latestPoint = if (points.isNotEmpty()) {
                LatLng(points.last().lat, points.last().lng)
            } else lastDriverLocation

            val arrow = vectorToBitmap(context, R.drawable.ic_navigation_arrow)

            AnimatedCarMarker(
                target = latestPoint,
                cameraFollow = true,
                arrowIcon = arrow,
                onUpdateCamera = { pos, brg ->
                    coroutineScope.launch {

                        val zoom = camera.position.zoom.takeIf { it > 0f } ?: 17f

                        val camPos = CameraPosition.Builder()
                            .target(pos)
                            .bearing(brg)
                            .zoom(zoom)
                            .tilt(45f)   // optional - navigation tilt
                            .build()

                        camera.animate(
                            CameraUpdateFactory.newCameraPosition(camPos),
                            300
                        )
                    }
                }
            )



            // DESTINATION MARKER
            destination?.let {
                Marker(
                    state = MarkerState(LatLng(it.lat, it.lng)),
                    title = "Destination",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                )
            }
        }
    }
}
