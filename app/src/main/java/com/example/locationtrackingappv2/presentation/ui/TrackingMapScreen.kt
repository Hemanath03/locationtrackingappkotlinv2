package com.example.locationtrackingappv2.presentation.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.locationtrackingappv2.domain.entity.PlaceSuggestion
import com.example.locationtrackingappv2.presentation.viewmodel.TrackingViewModel

@Composable
fun TrackingMapScreen(
    viewModel: TrackingViewModel
) {
    val points by viewModel.points.collectAsState()
    val destination by viewModel.destination.collectAsState()   // FIXED
    val fullRoute by viewModel.fullRoute.collectAsState()       // FIXED
    val remainingRoute by viewModel.remainingRoute.collectAsState() // FIXED

    val destSuggestions: List<PlaceSuggestion> =
        viewModel.destSuggestions.collectAsState().value

    val ctx = LocalContext.current

    var destText by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {

        AutocompleteField(
            value = destText,
            onValueChange = {
                destText = it
                viewModel.searchPlaces(it)
            },
            suggestions = destSuggestions,
            onSuggestionSelected = { suggestion ->
                destText = suggestion.description
                viewModel.onPlaceSelected(suggestion, ctx)
            },
            placeholder = "Enter Destination (Optional)",
            onClear = {
                destText = ""
                viewModel.clearDestination()
            }
        )

        TrackingMap(
            points = points,
            destination = destination,      // FIXED
            fullRoute = fullRoute,          // FIXED
            remainingRoute = remainingRoute, // FIXED
            modifier = Modifier.weight(1f)
        )
    }
}
