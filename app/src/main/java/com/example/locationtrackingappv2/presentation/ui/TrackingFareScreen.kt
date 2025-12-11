package com.example.locationtrackingappv2.presentation.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.locationtrackingappv2.presentation.viewmodel.TrackingViewModel
import androidx.compose.material3.*

@Composable
fun TrackingFareScreen(
    viewModel: TrackingViewModel
) {
    val fare = viewModel.fare.collectAsState().value
    val stats = viewModel.stats.collectAsState().value
    val remaining = viewModel.remainingDistance.collectAsState().value
    val estFare = viewModel.estimatedFare.collectAsState().value

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        Text("🚕 Trip Summary", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))

        val totalKm = (stats?.totalDistanceMeters ?: 0.0) / 1000.0
        val speedKmph = (stats?.instantSpeedMps ?: 0.0) * 3.6
        val avgKmph = (stats?.averageSpeedMps ?: 0.0) * 3.6

        Text("Total Distance Travelled: %.2f km".format(totalKm))
        Text("Current Speed: %.1f km/h".format(speedKmph))
        Text("Average Speed: %.1f km/h".format(avgKmph))

        Spacer(Modifier.height(12.dp))

        if (remaining != null) {
            Text("Remaining Distance: %.2f km".format(remaining / 1000))
        } else {
            Text("Remaining Distance: --")
        }

        Spacer(Modifier.height(12.dp))

        Text("Current Fare: ₹%.2f".format(fare))
        Text("Estimated Fare: ₹%.2f".format(estFare ?: 0.0))
    }
}

