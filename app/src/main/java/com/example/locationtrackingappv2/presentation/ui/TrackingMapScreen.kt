package com.example.locationtrackingappv2.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.locationtrackingappv2.presentation.viewmodel.TrackingViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

@Composable
fun TrackingMapScreen(
    viewModel: TrackingViewModel
) {
    val ctx = LocalContext.current

    // State from ViewModel
    val points by viewModel.points.collectAsState()
    val destination by viewModel.destination.collectAsState()
    val fullRoute by viewModel.fullRoute.collectAsState()
    val remainingRoute by viewModel.remainingRoute.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()

    val destSuggestions by viewModel.destSuggestions.collectAsState()
    var destText by remember { mutableStateOf("") }

    // Map load flag
    var mapLoaded by remember { mutableStateOf(false) }

    // GPS readiness flag
    val gpsReady = currentLocation != null

    // ------------------------------------------------------------------
    // CAMERA STATE + FLAGS
    // ------------------------------------------------------------------
    val camera = rememberCameraPositionState {
        // Neutral default camera (won’t be shown if GPS unavailable)
        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 17f)
    }

    var cameraInitialized by rememberSaveable { mutableStateOf(false) }
    var userMovedCamera by rememberSaveable { mutableStateOf(false) }
    var routeZoomed by rememberSaveable { mutableStateOf(false) }
    var lastCameraTarget by remember { mutableStateOf<LatLng?>(null) }

    val scope = rememberCoroutineScope()

    // ------------------------------------------------------------------
    // 1️⃣ FORCED INITIAL ZOOM — only when map ready + GPS ready
    // ------------------------------------------------------------------
    LaunchedEffect(mapLoaded, currentLocation?.lat, currentLocation?.lng) {
        val loc = currentLocation
        if (mapLoaded && gpsReady && !cameraInitialized && loc != null) {

            val pos = LatLng(loc.lat, loc.lng)

            // Double-move forces Google Maps to update UI instantly
            camera.move(CameraUpdateFactory.newLatLngZoom(pos, 17f))
            camera.move(CameraUpdateFactory.newLatLngZoom(pos, 17f))

            cameraInitialized = true
            userMovedCamera = false
        }
    }

    // ------------------------------------------------------------------
    // 2️⃣ AUTO FOLLOW — only when enabled and after map+GPS ready
    // ------------------------------------------------------------------
    LaunchedEffect(mapLoaded, currentLocation?.lat, currentLocation?.lng) {
        val loc = currentLocation
        if (mapLoaded && gpsReady && loc != null && !userMovedCamera) {

            val pos = LatLng(loc.lat, loc.lng)
            val zoom = if (camera.position.zoom > 0f) camera.position.zoom else 17f

            scope.launch {
                camera.animate(
                    CameraUpdateFactory.newLatLngZoom(pos, zoom),
                    durationMs = 350
                )
            }
        }
    }

    // ------------------------------------------------------------------
    // UI LAYOUT
    // ------------------------------------------------------------------
    Column(modifier = Modifier.fillMaxSize()) {

        // Show autocomplete only when both map and GPS ready
        val uiReady = mapLoaded && gpsReady

        if (uiReady) {
            AutocompleteField(
                value = destText,
                onValueChange = {
                    destText = it
                    viewModel.searchPlaces(it)
                },
                suggestions = destSuggestions,
                onSuggestionSelected = { s ->
                    destText = s.description
                    viewModel.onPlaceSelected(s, ctx)

                    routeZoomed = false
                    userMovedCamera = false
                },
                placeholder = "Enter Destination (Optional)",
                onClear = {
                    destText = ""
                    viewModel.clearDestination()
                    routeZoomed = false
                }
            )
        }

        // ------------------------------------------------------------------
        // DETECT USER DRAG / PAN / ZOOM
        // ------------------------------------------------------------------
        LaunchedEffect(camera.position) {
            val current = camera.position.target
            val previous = lastCameraTarget

            if (previous != null && current != previous && !camera.isMoving) {
                userMovedCamera = true
            }

            lastCameraTarget = current
        }

        // ------------------------------------------------------------------
        // ALWAYS SHOW MAP (do NOT hide even before GPS fix)
        // ------------------------------------------------------------------
        GoogleMap(
            modifier = Modifier.weight(1f),
            cameraPositionState = camera,
            properties = MapProperties(isMyLocationEnabled = true),
            onMapClick = { userMovedCamera = true },
            onMapLoaded = { mapLoaded = true }
        ) {

            // Driver path polyline
            if (points.isNotEmpty()) {
                Polyline(points = points.map { LatLng(it.lat, it.lng) })
            }

            // Full route (gray)
            if (fullRoute.isNotEmpty()) {
                Polyline(points = fullRoute, color = Color.Gray)
            }

            // Remaining route (blue)
            if (remainingRoute.isNotEmpty()) {
                Polyline(points = remainingRoute, color = Color.Blue)
            }

            // Destination marker
            destination?.let {
                Marker(state = MarkerState(position = LatLng(it.lat, it.lng)))
            }
        }

        // ------------------------------------------------------------------
        // 3️⃣ PERFECT ROUTE ZOOM EFFECT
        // ------------------------------------------------------------------
        LaunchedEffect(mapLoaded, fullRoute) {
            if (mapLoaded && fullRoute.isNotEmpty()) {

                routeZoomed = false

                val builder = com.google.android.gms.maps.model.LatLngBounds.Builder()
                fullRoute.forEach { builder.include(it) }
                val bounds = builder.build()

                snapshotFlow { camera.isMoving }.collect { moving ->

                    if (!moving && !routeZoomed) {

                        // Force immediately
                        camera.move(CameraUpdateFactory.newLatLngBounds(bounds, 200))

                        // Smooth animation after move
                        camera.animate(
                            CameraUpdateFactory.newLatLngBounds(bounds, 200),
                            durationMs = 600
                        )

                        routeZoomed = true
                        userMovedCamera = false
                    }
                }
            }
        }
    }
}
