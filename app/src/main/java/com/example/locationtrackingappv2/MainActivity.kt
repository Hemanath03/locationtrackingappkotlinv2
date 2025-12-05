package com.example.locationtrackingappv2

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.locationtrackingappv2.ui.theme.Locationtrackingappv2Theme
import com.example.locationtrackingappv2.ui.viewmodels.MainViewModel
import com.example.locationtrackingappv2.utils.PermissionManager
import com.example.locationtrackingappv2.utils.ServiceController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    // Permission launchers
    private val foregroundLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        if (PermissionManager.hasForegroundPermissions(this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                backgroundLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
        }
    }

    private val backgroundLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Background permission result handled
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            Locationtrackingappv2Theme {
                TaxiMeterScreen(
                    viewModel = viewModel,
                    onStartTracking = {
                        if (!PermissionManager.hasForegroundPermissions(this)) {
                            PermissionManager.requestForegroundPermissions(foregroundLauncher)
                        } else {
                            ServiceController.startService(this)
                        }
                    },
                    onStopTracking = {
                        ServiceController.stopService(this)
                    }
                )
            }
        }
    }
}

// ========================
//     MAIN SCREEN
// ========================
@Composable
fun TaxiMeterScreen(
    viewModel: MainViewModel,
    onStartTracking: () -> Unit,
    onStopTracking: () -> Unit
) {
    val locationData by viewModel.locationState.collectAsStateWithLifecycle()
    val pathPoints = remember { mutableStateListOf<LatLng>() }
    var isTracking by remember { mutableStateOf(false) }

    // Update path points when location changes
    LaunchedEffect(locationData) {
        locationData?.let { data ->
            val newPoint = LatLng(data.latitude, data.longitude)
            if (pathPoints.isEmpty() ||
                pathPoints.lastOrNull()?.let {
                    it.latitude != newPoint.latitude || it.longitude != newPoint.longitude
                } == true) {
                pathPoints.add(newPoint)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Map View - 60% of screen height
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.6f)
        ) {
            MapView(
                currentLocation = locationData?.let {
                    LatLng(it.latitude, it.longitude)
                },
                pathPoints = pathPoints.toList()
            )
        }

        // Styled Taxi Meter Panel - 40% of screen height
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.4f)
                .background(Color(0xFF1E1E2C))
        ) {
            TaxiMeterPanelStyled(
                locationData = locationData,
                isTracking = isTracking,
                onStartClick = {
                    isTracking = true
                    onStartTracking()
                },
                onStopClick = {
                    isTracking = false
                    onStopTracking()
                }
            )
        }
    }
}

// ========================
//       MAP VIEW
// ========================
@Composable
fun MapView(
    currentLocation: LatLng?,
    pathPoints: List<LatLng>
) {
    val defaultLocation = LatLng(-33.8523341, 151.2106085)
    val initialLocation = currentLocation ?: defaultLocation

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialLocation, 14f)
    }

    // Animate camera when location updates
    LaunchedEffect(currentLocation) {
        currentLocation?.let {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(it, 16f),
                durationMs = 600
            )
        }
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            isMyLocationEnabled = true,
            mapType = MapType.NORMAL
        ),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = true,
            myLocationButtonEnabled = true,
            compassEnabled = true
        )
    ) {
        // Draw path polyline
        if (pathPoints.size > 1) {
            Polyline(
                points = pathPoints,
                color = Color.Blue,
                width = 8f
            )
        }

        // Show current location marker
        currentLocation?.let {
            Marker(
                state = MarkerState(it),
                title = "You"
            )
        }
    }
}

// ========================
//     TAXI METER PANEL (STYLED)
// ========================
@Composable
fun TaxiMeterPanelStyled(
    locationData: com.example.locationtrackingappv2.models.LocationData?,
    isTracking: Boolean,
    onStartClick: () -> Unit,
    onStopClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // LEFT SECTION - Location Info + Start/Stop Buttons + Trip Info
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Location Information Display
            LocationInfoSection(locationData)

            Spacer(modifier = Modifier.height(8.dp))

            // Start/Stop Buttons
            ControlButtonsSection(
                isTracking = isTracking,
                onStartClick = onStartClick,
                onStopClick = onStopClick
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Trip Information
            TripInformationHeaderStyled(locationData)
        }

        Spacer(modifier = Modifier.width(16.dp))

        // RIGHT SECTION - Fare Breakdown + Extras + Tolls + Buttons
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // FARE
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "FARE $",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            "13.90",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Red
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text(
                                "M",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                            Text(
                                "04",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // EXTRAS & TOLLS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // EXTRAS
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "EXTRAS $",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "0.10",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Red
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row {
                        CircleButtonSmall("+", Color(0xFF3B3B4D))
                        Spacer(modifier = Modifier.width(8.dp))
                        CircleButtonSmall("-", Color(0xFF3B3B4D))
                    }
                }

                // TOLLS
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "TOLLS $",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        if (locationData?.isTollRoad == true) "1.30" else "0.00",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (locationData?.isTollRoad == true) Color.Red else Color.Gray
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        if (locationData?.isTollRoad == true) "1 SET" else "0 SET",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // TOLL DETAILS
            if (locationData?.isTollRoad == true) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        "🚧 TOLL ROAD DETECTED!",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Cyan
                    )
                        }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Tolls Details",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Cyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ACTION BUTTONS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlineButtonSmallStyled("Pause")
                OutlineButtonSmallStyled("Wait")
                OutlineButtonSmallStyled("Meter")
                    }
                }
            }
        }

// ========================
// LOCATION INFO SECTION
// ========================
@Composable
fun LocationInfoSection(locationData: com.example.locationtrackingappv2.models.LocationData?) {
    Column {
        Text(
            "Location Info",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(4.dp))

        if (locationData != null) {
            Text(
                "Lat: ${String.format("%.6f", locationData.latitude)}",
                fontSize = 11.sp,
                color = Color.Gray
            )
            Text(
                "Lng: ${String.format("%.6f", locationData.longitude)}",
                fontSize = 11.sp,
                color = Color.Gray
            )
            Text(
                "Speed: ${String.format("%.2f", locationData.speed)} m/s",
                fontSize = 11.sp,
                color = Color.Gray
            )
            Text(
                "Speed: ${String.format("%.2f", locationData.speed * 3.6)} km/h",
                fontSize = 11.sp,
                color = Color.Gray
            )
            if (locationData.isTollRoad) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    "🚧 TOLL ROAD",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Cyan
                )
            }
        } else {
            Text(
                "Location not available",
                fontSize = 11.sp,
                color = Color.Gray
            )
        }
    }
}

// ========================
// CONTROL BUTTONS SECTION
// ========================
@Composable
fun ControlButtonsSection(
    isTracking: Boolean,
    onStartClick: () -> Unit,
    onStopClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Start Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(if (isTracking) Color.Gray else Color(0xFF4CAF50))
                .then(
                    if (!isTracking) {
                        Modifier.clickable { onStartClick() }
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Start Tracking",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Stop Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(if (isTracking) Color(0xFFF44336) else Color.Gray)
                .then(
                    if (isTracking) {
                        Modifier.clickable { onStopClick() }
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Stop Tracking",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

// ========================
// LEFT SECTION STYLED COMPONENTS
// ========================
@Composable
fun TripInformationHeaderStyled(locationData: com.example.locationtrackingappv2.models.LocationData?) {
    Column {
        val labelColor = Color.White
        val grayColor = Color.Gray

        Text(
            "On Trip NO : 3",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = labelColor
        )
        Text(
            "Booking NO : 14511",
            fontSize = 10.sp,
            color = grayColor
        )
        Text(
            "Requested : Apr 05 - 20:12",
            fontSize = 10.sp,
            color = grayColor
        )
        Text(
            "From : SoftClient",
            fontSize = 10.sp,
            color = grayColor
        )
        Text(
            "Meter on : Apr 05 - 20:12",
            fontSize = 10.sp,
            color = grayColor
        )
        
        // Calculate distance if we have location data
        val distance = if (locationData != null && locationData.speed > 0) {
            // Simple calculation - in real app, calculate from path points
            "0.0"
        } else {
            "0.0"
        }
        
        Text(
            "Duration : 4.34 (\$ 7.1)",
            fontSize = 10.sp,
            color = grayColor
        )
        Text(
            "Distance : $distance km (\$ 0.0)",
            fontSize = 10.sp,
            color = grayColor
        )
    }
}

@Composable
fun AddressSectionStyled() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left Column: Number + Pick Up
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("1", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Pick Up", fontSize = 10.sp, color = Color.White)
        }

        // Middle Column: Address Lines
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                "10, Waterfall Cres, Bella Vista",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                "NSW 2153, Australia",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // Right Column: Navigation Icon + Text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Placeholder for Nav Icon
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
            Text("Navigate", fontSize = 10.sp, color = Color.White)
        }
    }
}

@Composable
fun CancelButtonStyled() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color.DarkGray),
        contentAlignment = Alignment.Center
    ) {
        Text("Cancel", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

// ========================
// BUTTON COMPONENTS
// ========================
@Composable
fun CircleButtonSmall(text: String, color: Color) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
fun OutlineButtonSmallStyled(text: String) {
    Box(
        modifier = Modifier
            .height(32.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF3B3B4D))
            .border(1.dp, Color.Gray, RoundedCornerShape(6.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}
