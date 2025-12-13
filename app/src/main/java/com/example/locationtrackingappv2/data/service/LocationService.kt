package com.example.locationtrackingappv2.data.service

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.locationtrackingappv2.R
import com.example.locationtrackingappv2.data.repository.LocationRepositoryImpl
import com.example.locationtrackingappv2.domain.entity.LocationPoint
import com.example.locationtrackingappv2.domain.entity.LocationStats
import com.example.locationtrackingappv2.domain.usecase.ObserveLocationStatsUseCase
import com.google.android.gms.location.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class LocationService : Service() {

    @Inject
    lateinit var locationRepository: LocationRepositoryImpl

    @Inject
    lateinit var observeLocationStatsUseCase: ObserveLocationStatsUseCase

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val fusedClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    private val locationRequest: LocationRequest =
        LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            2000L
        )
            .setMinUpdateIntervalMillis(1000L)
            .setMaxUpdateDelayMillis(3000L)
            .build()

    private lateinit var notificationBuilder: NotificationCompat.Builder
    private val notificationManager by lazy { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    override fun onCreate() {
        super.onCreate()
        notificationBuilder = buildNotificationBuilder("Tracking active…")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                // start foreground with initial notification
                startForeground(NOTIFICATION_ID, notificationBuilder.build())
                startLocationUpdates()
                startStatsObserver()
            }
            ACTION_STOP -> {
                stopStatsObserver()
                stopLocationUpdates()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    // -----------------------------
    // Location updates functions
    // -----------------------------
    @Suppress("MissingPermission") // permission checked at runtime below
    private fun startLocationUpdates() {
        if (!hasLocationPermission()) {
            stopSelf()
            return
        }

        try {
            fusedClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        } catch (se: SecurityException) {
            se.printStackTrace()
            stopSelf()
        }
    }

    private fun stopLocationUpdates() {
        try {
            fusedClient.removeLocationUpdates(locationCallback)
        } catch (se: SecurityException) {
            se.printStackTrace()
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.locations.forEach { loc ->
                val point = LocationPoint(lat = loc.latitude, lng = loc.longitude, timestamp = loc.time)
                // publish without blocking
                //Log.i("LocationService", "Publishing location: $point ${loc.speed}");
                locationRepository.publishLocationNonBlocking(point)
            }
        }
    }

    // -----------------------------
    // Stats observer -> updates notification
    // -----------------------------
    private var statsJob: Job? = null

    private fun startStatsObserver() {
        // avoid multiple collectors
        if (statsJob?.isActive == true) return

        statsJob = serviceScope.launch {
            // Observe the location stats flow (usecase provides Flow<LocationStats>)
            try {
                observeLocationStatsUseCase().collectLatest { stats ->
                    updateNotificationWithStats(stats)
                }
            } catch (t: CancellationException) {
                throw t
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }
    }

    private fun stopStatsObserver() {
        statsJob?.cancel()
        statsJob = null
    }

    // -----------------------------
    // Notification helpers
    // -----------------------------
    private fun buildNotificationBuilder(initialText: String): NotificationCompat.Builder {
        val stopIntent = Intent(this, LocationService::class.java).apply { action = ACTION_STOP }
        val stopPending = PendingIntent.getService(
            this,
            0,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, NOTIF_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle("Ride in progress")
            .setContentText(initialText)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopPending)
    }

    private fun updateNotificationWithStats(stats: LocationStats) {
        // Format distance and speed
        val distanceText = if (stats.totalDistanceMeters >= 1000) {
            String.format("%.2f km", stats.totalDistanceMeters / 1000.0)
        } else {
            String.format("%.0f m", stats.totalDistanceMeters)
        }

        val speedKmph = stats.instantSpeedMps * 3.6
        val speedText = String.format("%.1f km/h", speedKmph)

        val content = "Dist: $distanceText • Speed: $speedText"

        // update builder and notify
        notificationBuilder.setContentText(content)
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    // -----------------------------
    // Permission helper
    // -----------------------------
    private fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        return fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        stopStatsObserver()
        serviceScope.cancel()
    }

    companion object {
        const val ACTION_START = "LocationService.action.START"
        const val ACTION_STOP = "LocationService.action.STOP"
        const val NOTIF_CHANNEL_ID = "location_channel"
        const val NOTIFICATION_ID = 101
    }
}
