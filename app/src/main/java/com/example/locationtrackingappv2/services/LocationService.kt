package com.example.locationtrackingappv2.services

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.IBinder
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.locationtrackingappv2.data.repository.LocationRepository
import com.example.locationtrackingappv2.domain.repository.IRouteStore
import com.example.locationtrackingappv2.domain.repository.ITollRepository
import com.example.locationtrackingappv2.domain.util.INotificationHelper
import com.example.locationtrackingappv2.domain.util.NotificationHelperImpl
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import org.json.JSONArray
import javax.inject.Inject

@AndroidEntryPoint
class LocationService @Inject constructor() : Service() {

    @Inject lateinit var fusedClient: FusedLocationProviderClient
    @Inject lateinit var locationRepository: LocationRepository   // existing repo for UI updates
    @Inject lateinit var tollRepository: ITollRepository          // now depends on interface
    @Inject lateinit var routeStore: IRouteStore                  // stores planned route
    @Inject lateinit var notificationHelper: INotificationHelper  // notification abstraction

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private lateinit var callback: LocationCallback
    private val enteredTollSegments = mutableSetOf<Int>()

    override fun onCreate() {
        super.onCreate()
        notificationHelper.createNotificationChannelIfNeeded()
        startForeground(NotificationHelperImpl.NOTIF_ID, notificationHelper.buildNotification("Waiting for route..."))

        serviceScope.launch {
            // load tolls via interface
            try {
                tollRepository.loadTolls(this@LocationService)
            } catch (t: Throwable) {
                Log.w(TAG, "Toll load failed: ${t.message}")
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Accept route via Intent (backwards compatible), but prefer routeStore API
        intent?.getStringExtra("ROUTE_POINTS")?.let { json ->
            val points = decodeRouteJson(json)
            routeStore.setRoute(points)
            notificationHelper.updateNotification(NotificationHelperImpl.NOTIF_ID, notificationHelper.buildNotification("Route loaded. Tracking started."))
        }

        startLocationUpdates()
        return START_STICKY
    }

    private fun decodeRouteJson(json: String): List<LatLng> {
        val arr = JSONArray(json)
        val list = ArrayList<LatLng>(arr.length())
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            list.add(LatLng(obj.getDouble("latitude"), obj.getDouble("longitude")))
        }
        return list
    }

    private fun startLocationUpdates() {
        val req = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000L)
            .setMinUpdateIntervalMillis(1000L)
            .build()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Missing permission")
            return
        }

        callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                for (loc in result.locations) {
                    serviceScope.launch { handleLocation(loc) }
                }
            }
        }

        fusedClient.requestLocationUpdates(req, callback, mainLooper)
    }

    private suspend fun handleLocation(loc: Location) = withContext(Dispatchers.Default) {
        val latLng = LatLng(loc.latitude, loc.longitude)

        // 1) real-time toll
        val isToll = tollRepository.isPointOnToll(loc.latitude, loc.longitude)

        // 2) planned route (from routeStore)
        val planned = routeStore.getRoute()
        val onRoute = planned?.let { PolyUtil.isLocationOnPath(latLng, it, false, 40.0) } ?: false

        // 3) detect toll entry using tollRepository -> returns domain TollMatch list
        detectTollEntry(latLng, planned)

        // 4) update UI repo (unchanged)
        locationRepository.updateLocation(loc.latitude, loc.longitude, loc.speed, isToll)

        // 5) update notification
        val msg = when {
            isToll -> "⚠️ On Toll Road"
            onRoute -> "On planned route"
            else -> "Off route — recalculation recommended"
        }
        notificationHelper.updateNotification(NotificationHelperImpl.NOTIF_ID, notificationHelper.buildNotification(msg))
    }

    private fun detectTollEntry(point: LatLng, planned: List<LatLng>?) {
        if (planned == null) return
        val tolls = tollRepository.getTollsIntersecting(planned)
        for (t in tolls) {
            if (!enteredTollSegments.contains(t.segmentId)) {
                if (PolyUtil.isLocationOnPath(point, t.segmentPoints, false, 40.0)) {
                    enteredTollSegments.add(t.segmentId)
                    notificationHelper.updateNotification(NotificationHelperImpl.NOTIF_ID, notificationHelper.buildNotification("🚧 Entered Toll Segment"))
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try { fusedClient.removeLocationUpdates(callback) } catch (_: Throwable) {}
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val TAG = "LocationService"
    }
}
