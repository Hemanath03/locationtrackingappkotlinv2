package com.example.locationtrackingappv2.presentation.ui

import androidx.compose.runtime.*
import com.example.locationtrackingappv2.utils.computeBearing
import com.example.locationtrackingappv2.utils.lerpLatLng
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import kotlinx.coroutines.delay

@Composable
fun AnimatedCarMarker(
    target: LatLng?,
    cameraFollow: Boolean = true,
    onUpdateCamera: (LatLng, Float) -> Unit,
    arrowIcon: BitmapDescriptor
) {
    var currentPos by remember { mutableStateOf(target ?: LatLng(0.0, 0.0)) }
    var oldPos by remember { mutableStateOf(currentPos) }
    var bearing by remember { mutableStateOf(0f) }

    // Animate smoothly between oldPos -> target
    LaunchedEffect(target) {
        if (target == null) return@LaunchedEffect

        oldPos = currentPos
        val duration = 300L
        val frameTime = 16L
        val steps = (duration / frameTime).toInt().coerceAtLeast(1)

        for (i in 1..steps) {
            val t = i / steps.toFloat()
            currentPos = lerpLatLng(oldPos, target, t)
            bearing = computeBearing(oldPos, target)

            if (cameraFollow) onUpdateCamera(currentPos, bearing)
            delay(frameTime)
        }

        currentPos = target
        bearing = computeBearing(oldPos, target)
        if (cameraFollow) onUpdateCamera(currentPos, bearing)
    }

    Marker(
        state = MarkerState(currentPos),
        title = "Driver",
        icon = arrowIcon,
        rotation = bearing,
        flat = true      // ensures arrow rotates with bearing
    )
}
