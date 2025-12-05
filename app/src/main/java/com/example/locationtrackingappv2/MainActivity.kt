package com.example.locationtrackingappv2

import android.os.Bundle
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.locationtrackingappv2.controller.AutoCompleteController
import com.example.locationtrackingappv2.controller.MapController
import com.example.locationtrackingappv2.controller.TripNavigator
import com.example.locationtrackingappv2.presentation.viewmodel.TripViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val vm: TripViewModel by viewModels()

    private lateinit var mapController: MapController
    private lateinit var autoController: AutoCompleteController
    private lateinit var navigator: TripNavigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mapController = MapController(this)
        autoController = AutoCompleteController(this, vm)
        navigator = TripNavigator(this)

        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            vm.route.collect { route ->
                if (route != null) mapController.drawRoute(route.polylinePoints)
            }
        }

        lifecycleScope.launch {
            vm.predictions.collect { predictions ->
                autoController.showPredictions(predictions)
            }
        }

        lifecycleScope.launch {
            vm.events.collect { event -> autoController.showEvent(event) }
        }
    }

    private fun setupListeners() {
        autoController.onFromTextChanged = { vm.onFromQuery(it) }
        autoController.onToTextChanged   = { vm.onToQuery(it) }

        autoController.onFromSelected = { vm.selectPrediction(it, true) }
        autoController.onToSelected   = { vm.selectPrediction(it, false) }

        findViewById<Button>(R.id.btnCompute).setOnClickListener {
            vm.computeRoute()
        }

        findViewById<Button>(R.id.btnStartTrip).setOnClickListener {
            vm.route.value?.polylinePoints?.let { points ->
                navigator.startTrip(points)
            }
        }
    }
}
