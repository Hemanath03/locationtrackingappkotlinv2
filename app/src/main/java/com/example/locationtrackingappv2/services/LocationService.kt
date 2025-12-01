package com.example.locationtrackingappv2.services

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.locationtrackingappv2.R
import com.example.locationtrackingappv2.data.LocationRepository
import com.example.locationtrackingappv2.data.TollRepository
import com.google.android.gms.location.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class LocationService : Service() {

    @Inject lateinit var fusedClient: FusedLocationProviderClient
    @Inject lateinit var repository: LocationRepository
    @Inject lateinit var tollRepo: TollRepository


    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private lateinit var locationCallback: LocationCallback

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "SERVICE CREATED")
        createNotificationChannelIfNeeded()
        startForeground(NOTIFICATION_ID, buildNotification())
        serviceScope.launch {
            tollRepo.loadTollData(this@LocationService)
        }
        startLocationUpdates()
    }

    private fun createNotificationChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Location Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows when location tracking is active"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Tracking Active")
            .setContentText("Location service is running")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .build()
    }

    private fun startLocationUpdates() {
        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 5000L
        ).setMinUpdateIntervalMillis(2000L).build()

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(TAG, "Missing ACCESS_FINE_LOCATION - not starting updates")
            return
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                for (location in result.locations) {
                    serviceScope.launch {
                        handleLocation(location)
                    }
                }
            }
        }
        fusedClient.requestLocationUpdates(request, locationCallback, mainLooper)
    }

    private suspend fun handleLocation(location: Location) = withContext(Dispatchers.IO) {
        Log.d(TAG, "Location: ${location.latitude}, ${location.longitude}")
        val isToll = tollRepo.isNearTollRoad(location.latitude, location.longitude)
        repository.updateLocation(location.latitude, location.longitude, location.speed,  isToll)

    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            fusedClient.removeLocationUpdates(locationCallback)
        } catch (e: Exception) {
            Log.w(TAG, "removeLocationUpdates failed: ${e.message}")
        }
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val TAG = "LocationService"
        const val CHANNEL_ID = "location_tracking_channel"
        const val NOTIFICATION_ID = 1
    }
}
