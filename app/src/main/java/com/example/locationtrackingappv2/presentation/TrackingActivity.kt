package com.example.locationtrackingappv2.presentation


import TrackingMainScreen
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.locationtrackingappv2.data.service.LocationService
import com.example.locationtrackingappv2.presentation.viewmodel.TrackingViewModel
import dagger.hilt.android.AndroidEntryPoint
import android.util.Log


@AndroidEntryPoint
class TrackingActivity : ComponentActivity() {


    private val vm: TrackingViewModel by viewModels()
    private val TAG = "TrackingActivity"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


// Start LocationService immediately so GPS is available before meter starts
        try {
            val intent = Intent(this, LocationService::class.java).apply {
                action = LocationService.ACTION_START
            }


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }


            Log.d(TAG, "LocationService started on activity create")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "Failed to start LocationService on create: ${e.message}")
        }


        setContent {
            TrackingMainScreen(vm)
        }

    }
}