
// File: TrackingScreen.kt
package com.example.locationtrackingappv2.presentation.ui

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.locationtrackingappv2.presentation.viewmodel.TrackingViewModel

@Composable
fun TrackingScreen(
    viewModel: TrackingViewModel,
    onStartClick: () -> Unit,
    onStopClick: () -> Unit
) {
    val ctx = LocalContext.current

    val points by viewModel.points.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val dest by viewModel.destination.collectAsState()
    val destSuggestions by viewModel.destSuggestions.collectAsState()
    val fullRoute by viewModel.fullRoute.collectAsState()
    val remainingRoute by viewModel.remainingRoute.collectAsState()

    val remaining by viewModel.remainingDistance.collectAsState()
    val fare by viewModel.fare.collectAsState()
    val estFare by viewModel.estimatedFare.collectAsState()
    val running by viewModel.meterRunning.collectAsState()

    var destText by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(12.dp)) {
        AutocompleteField(
            value = destText,
            onValueChange = {
                destText = it
                viewModel.searchPlaces(it)
            },
            suggestions = destSuggestions,
            onSuggestionSelected = { s ->
                destText = s.description
                // pass context so ViewModel can attempt immediate GPS fallback
                viewModel.onPlaceSelected(s, ctx)
            },
            placeholder = "Enter Destination (Optional)",
            onClear = {
                destText = ""
                viewModel.clearDestination()
            }
        )

        Spacer(Modifier.height(8.dp))

        TrackingMap(
            points = points,
            destination = dest,
            fullRoute = fullRoute,
            remainingRoute = remainingRoute,
            modifier = Modifier.weight(1f)
        )


        Spacer(Modifier.height(12.dp))

        Row(Modifier.fillMaxWidth()) {
            Button(
                modifier = Modifier.weight(1f),
                enabled = !running,
                onClick = onStartClick
            ) { Text("Start Meter") }

            Spacer(Modifier.width(8.dp))

            Button(
                modifier = Modifier.weight(1f),
                enabled = running,
                onClick = onStopClick
            ) { Text("Stop Meter") }
        }

        Spacer(Modifier.height(10.dp))

        stats?.let {
            Text("Distance: %.2f km".format(it.totalDistanceMeters / 1000.0))
            Text("Speed: %.1f km/h".format(it.instantSpeedMps * 3.6))
        }

        Text("Fare: $%.2f".format(fare))

        remaining?.let {
            if (dest != null) Text("Remaining: %.2f km".format(it / 1000.0))
        }

        estFare?.let {
            Text("Estimated Fare: $%.2f".format(it))
        }
    }
}
