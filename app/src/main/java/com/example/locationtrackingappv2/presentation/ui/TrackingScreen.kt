package com.example.locationtrackingappv2.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.locationtrackingappv2.presentation.viewmodel.TrackingViewModel

@Composable
fun TrackingScreen(
    viewModel: TrackingViewModel,
    onStartClick: () -> Unit,
    onStopClick: () -> Unit
) {
    val points by viewModel.points.collectAsState()
    val stats by viewModel.stats.collectAsState()

    val originPoint by viewModel.originPoint.collectAsState()
    val destPoint by viewModel.destPoint.collectAsState()

    val originSuggestions by viewModel.originSuggestions.collectAsState()
    val destSuggestions by viewModel.destSuggestions.collectAsState()

    var originText by remember { mutableStateOf("") }
    var destText by remember { mutableStateOf("") }

    var followUser by remember { mutableStateOf(true) }

    Column(Modifier.fillMaxSize().padding(12.dp)) {

        AutocompleteField(
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
            placeholder = "Origin"
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
            placeholder = "Destination"
        )

        Spacer(Modifier.height(8.dp))

        TrackingMap(
            points = points,
            origin = originPoint,
            destination = destPoint,
            followUser = followUser,
            onToggleFollow = { followUser = !followUser },
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
    }
}
