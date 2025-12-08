package com.example.locationtrackingappv2.presentation.ui

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
    val context = LocalContext.current
    val points by viewModel.points.collectAsState()
    val stats by viewModel.stats.collectAsState()

    val originPoint by viewModel.originPoint.collectAsState()
    val destPoint by viewModel.destPoint.collectAsState()

    val originSuggestions by viewModel.originSuggestions.collectAsState()
    val destSuggestions by viewModel.destSuggestions.collectAsState()

    val remaining by viewModel.remainingDistanceMeters.collectAsState()

    var originText by remember { mutableStateOf("") }
    var destText by remember { mutableStateOf("") }

    val routePoints by viewModel.routePoints.collectAsState()

    val routeDistance by viewModel.routeDistanceMeters.collectAsState()
    val routeDuration by viewModel.routeDurationSeconds.collectAsState()

    Column(Modifier.fillMaxSize().padding(12.dp)) {


        OriginField(
            value = originText,
            onValueChange = {
                originText = it
                viewModel.searchPlaces(it, true)
            },
            suggestions = originSuggestions,
            onSuggestionSelected = { s ->
                originText = s.description
                viewModel.onPlaceSelected(s, true)
            },
            onUseCurrentLocation = {
                originText = ""
                viewModel.setOriginFromCurrentLocation(context =context )
            },
                    onClear = {
                originText = ""
                viewModel.clearOrigin()
            }
        )

        Spacer(Modifier.height(8.dp))

        AutocompleteField(
            value = destText,
            onValueChange = {
                destText = it
                viewModel.searchPlaces(it, false)
            },
            suggestions = destSuggestions,
            onSuggestionSelected = { s ->
                destText = s.description
                viewModel.onPlaceSelected(s, false)
            },
            placeholder = "Destination",
            onClear = {
                destText = ""
                viewModel.clearDestination()
            }
        )

        Spacer(Modifier.height(8.dp))


        TrackingMap(
            points = points,
            origin = originPoint,
            destination = destPoint,
            route = routePoints,
            modifier = Modifier.weight(1f)
        )

        Spacer(Modifier.height(8.dp))

        Row(Modifier.fillMaxWidth()) {
            Button(onClick = onStartClick, modifier = Modifier.weight(1f)) {
                Text("Start")
            }
            Spacer(Modifier.width(8.dp))
            Button(onClick = onStopClick, modifier = Modifier.weight(1f)) {
                Text("Stop")
            }
        }

        Spacer(Modifier.height(8.dp))

        stats?.let {
            Text("Distance: %.2f km".format(it.totalDistanceMeters / 1000))
            Text("Speed: %.1f km/h".format(it.instantSpeedMps * 3.6))
        }
        remaining?.let {
            if (destPoint != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Remaining Distance: %.2f km".format(it / 1000),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        routeDistance?.let {
            Text("Route Distance: %.2f km".format(it / 1000f))
        }

        routeDuration?.let {
            val minutes = it / 60
            val hours = minutes / 60
            val mins = minutes % 60

            Text("ETA: ${hours}h ${mins}m")
        }
    }
}
