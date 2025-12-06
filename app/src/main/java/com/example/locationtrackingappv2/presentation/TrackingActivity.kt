package com.example.locationtrackingappv2.presentation

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.example.locationtrackingappv2.data.service.LocationService
import com.example.locationtrackingappv2.presentation.ui.TrackingScreen
import com.example.locationtrackingappv2.presentation.viewmodel.TrackingViewModel
import dagger.hilt.android.AndroidEntryPoint
import android.util.Log


@AndroidEntryPoint
class TrackingActivity : ComponentActivity() {

    private val vm: TrackingViewModel by viewModels()
private  val tag = "";
    // Request fine location at runtime
    private val requestFineLocation =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            Log.d(tag,"Fine location granted=$granted")
        }

    // On Android 11+ may need background location separately (user flow); provide info only
    private val requestBackgroundLocation =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            Log.d(tag,"Background location granted=$granted")
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ask permission on first open
        requestFineLocation.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Optional: explain to user then request background
            // requestBackgroundLocation.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }

        setContent {
            MaterialTheme {
                TrackingScreen(
                    viewModel = vm,
                    onStartClick = {
                        val intent = Intent(this, LocationService::class.java).apply {
                            action = LocationService.ACTION_START
                        }

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            startForegroundService(intent)
                        } else {
                            startService(intent)
                        }
                    },
                    onStopClick = {
                        val intent = Intent(this, LocationService::class.java).apply {
                            action = LocationService.ACTION_STOP
                        }
                        startService(intent)
                    }
                )
            }
        }
    }
}
