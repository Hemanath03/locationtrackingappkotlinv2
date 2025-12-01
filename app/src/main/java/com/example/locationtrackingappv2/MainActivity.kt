package com.example.locationtrackingappv2


import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.locationtrackingappv2.ui.viewmodels.MainViewModel
import com.example.locationtrackingappv2.utils.PermissionManager
import com.example.locationtrackingappv2.utils.ServiceController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.CameraUpdateFactory


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), OnMapReadyCallback  {

    private val viewModel: MainViewModel by viewModels()

    private lateinit var txtLocation: TextView
    private lateinit var btnStart: Button
    private lateinit var btnStop: Button
    private lateinit var map: GoogleMap
    private var userMarker: Marker? = null
    private var polyline: Polyline? = null
    private val pathPoints = mutableListOf<LatLng>()


    // Foreground permission launcher (request fine & coarse)
    private val foregroundLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        // If foreground granted, ask for background in Android 10+ if desired
        if (PermissionManager.hasForegroundPermissions(this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Request background separately (best practice)
                backgroundLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
        }
    }

    private val backgroundLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        // nothing extra needed; user might need to go to settings if denied permanently
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)


        txtLocation = findViewById(R.id.txtLocation)
        btnStart = findViewById(R.id.btnStart)
        btnStop = findViewById(R.id.btnStop)

        observeViewModel()

        btnStart.setOnClickListener {
            if (!PermissionManager.hasForegroundPermissions(this)) {
                PermissionManager.requestForegroundPermissions(foregroundLauncher)
                return@setOnClickListener
            }
            // optionally ensure background permission before starting (if you need)
            ServiceController.startService(this)
        }

        btnStop.setOnClickListener {
            ServiceController.stopService(this)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isZoomControlsEnabled = true
    }


    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.locationState.collect { data ->
                    if (data != null) {
                        txtLocation.text = "Lat: ${data.latitude}\nLng: ${data.longitude}\nSpeed: ${"%.2f".format(data.speed)} m/s"
                        if (data.isTollRoad) {
                            txtLocation.append("\n🚧 TOLL ROAD DETECTED!")
                        }

                        updateMap(data.latitude, data.longitude)
                    } else {
                        txtLocation.text = "Location not available"
                    }
                }
            }
        }
    }
    private fun updateMap(lat: Double, lng: Double) {
        val newPoint = LatLng(lat, lng)

        // Move camera
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(newPoint, 16f))

        // Update marker
        if (userMarker == null) {
            userMarker = map.addMarker(MarkerOptions().position(newPoint).title("You"))
        } else {
            userMarker!!.position = newPoint
        }

        // Update polyline path
        pathPoints.add(newPoint)

        if (polyline == null) {
            polyline = map.addPolyline(
                PolylineOptions().addAll(pathPoints)
            )
        } else {
            polyline!!.points = pathPoints
        }
    }

}
