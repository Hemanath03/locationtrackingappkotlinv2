package com.example.locationtrackingappv2.presentation.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.locationtrackingappv2.presentation.viewmodel.TrackingViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

val DarkGrey = Color(0xFF2C2C2C)
val RedDigital = Color(0xFFFF0000)
val GreenButton = Color(0xFF4CAF50)
val RedButton = Color(0xFFF44336)
val DisabledButton = Color(0xFF1A1A1A)

@SuppressLint("DefaultLocale")
@Composable
fun TrackingScreen(
    viewModel: TrackingViewModel,
    onStartClick: () -> Unit,
    onStopClick: () -> Unit
) {
    val context = LocalContext.current
    val points by viewModel.points.collectAsState()
    val stats by viewModel.stats.collectAsState()

    val originPoint by viewModel.originPoint.collectAsState()
    val destPoint by viewModel.destPoint.collectAsState()

    val originSuggestions by viewModel.originSuggestions.collectAsState()
    val destSuggestions by viewModel.destSuggestions.collectAsState()

    val remaining by viewModel.remainingDistanceMeters.collectAsState()

    var originText by remember { mutableStateOf("") }
    var destText by remember { mutableStateOf("") }
    
    // Sync text fields with selected places
    LaunchedEffect(originPoint) {
        if (originPoint != null && originText.isEmpty()) {
            // Don't override if user is typing
        }
    }
    
    LaunchedEffect(destPoint) {
        if (destPoint != null && destText.isEmpty()) {
            // Don't override if user is typing
        }
    }

    val routePoints by viewModel.routePoints.collectAsState()

    val routeDistance by viewModel.routeDistanceMeters.collectAsState()

    // Track if tracking has started
    var isTrackingStarted by remember { mutableStateOf(false) }
    var tripStartTime by remember { mutableStateOf<Long?>(null) }
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    
    // Update current time every second
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTime = System.currentTimeMillis()
        }
    }
    
    // Calculate trip duration
    val tripDurationSeconds = if (tripStartTime != null) {
        ((currentTime - tripStartTime!!) / 1000).coerceAtLeast(0)
    } else {
        0L
    }
    
    // Distance in km
    val distanceKm = stats?.totalDistanceMeters?.div(1000.0) ?: 0.0
    
    // Load route when origin and destination are set
    LaunchedEffect(originPoint, destPoint) {
        if (originPoint != null && destPoint != null) {
            viewModel.loadRoute()
        }
    }
    
    // Handle start click
    val handleStartClick = {
        if (!isTrackingStarted) {
            isTrackingStarted = true
            tripStartTime = System.currentTimeMillis()
            currentTime = System.currentTimeMillis()
        }
        onStartClick()
    }
    
    // Handle stop click
    val handleStopClick = {
        isTrackingStarted = false
        tripStartTime = null
        onStopClick()
    }

    Column(Modifier.fillMaxSize()) {
        
        // 1. Text fields - At the top
        Column(
            modifier = Modifier
                .background(DarkGrey)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            OriginField(
                value = originText,
                onValueChange = {
                    originText = it
                    viewModel.searchPlaces(it, true)
                },
                suggestions = originSuggestions,
                onSuggestionSelected = { s ->
                    originText = s.description
                    viewModel.onPlaceSelected(s, true)
                },
                onUseCurrentLocation = {
                    originText = "Current Location"
                    viewModel.setOriginFromCurrentLocation(context = context)
                },
                onClear = {
                    originText = ""
                    viewModel.clearOrigin()
                }
            )

            Spacer(Modifier.height(4.dp))

            AutocompleteField(
                value = destText,
                onValueChange = {
                    destText = it
                    viewModel.searchPlaces(it, false)
                },
                suggestions = destSuggestions,
                onSuggestionSelected = { s ->
                    destText = s.description
                    viewModel.onPlaceSelected(s, false)
                },
                placeholder = "Destination",
                onClear = {
                    destText = ""
                    viewModel.clearDestination()
                }
            )
        }

        // 2. Map - 2.3/4
        TrackingMap(
            points = points,
            origin = originPoint,
            destination = destPoint,
            route = routePoints,
            modifier = Modifier.weight(2.1f)
        )

        // 3. Taxi Meter UI - 1/4 (Always shown)
        Box(
            modifier = Modifier
                .weight(1.2f)
                .fillMaxWidth()
                .background(DarkGrey)
        ) {
            // Taxi Meter UI
                val baseFare = 3.50
                val perKmRate = 1.50
                val perMinuteRate = 0.25
                val distanceCost = distanceKm * perKmRate
                val durationMinutes = tripDurationSeconds / 60.0
                val timeCost = durationMinutes * perMinuteRate
                val totalFare = baseFare + distanceCost + timeCost

                val extras = remember { mutableStateOf(0.10) }
                val tolls = remember { mutableStateOf(1.30) }

                val dateFormat = SimpleDateFormat("MMM dd - HH:mm", Locale.getDefault())
                val formattedDuration = formatDuration(tripDurationSeconds)
                val durationCostValue = (durationMinutes * perMinuteRate).coerceAtLeast(0.0)
                val meterOnTimeValue = tripStartTime ?: System.currentTimeMillis()
                val originAddressValue = originText.ifBlank { null }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                ) {
                    // Left Section - Trip Details
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "Meter on : ${dateFormat.format(Date(meterOnTimeValue))}",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )

                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = "Duration : $formattedDuration ($${String.format("%.1f", durationCostValue)})",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "Distance : ${String.format("%.1f", distanceKm)} km ($${String.format("%.1f", distanceCost)})",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                        Spacer(Modifier.height(2.dp))
                        
                        // Route Distance
                        routeDistance?.let {
                            Text(
                                text = "Route Distance : ${String.format("%.2f", it / 1000f)} km",
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                            Spacer(Modifier.height(2.dp))
                        }
                        
                        // Remaining Distance
                        remaining?.let {
                            if (destPoint != null) {
                                Text(
                                    text = "Remaining : ${String.format("%.2f", it / 1000)} km",
                                    color = Color.Gray,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(Modifier.height(4.dp))
                        HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.5f))
                        Spacer(Modifier.height(4.dp))

                        if (originAddressValue != null) {
                            Text(
                                text = originAddressValue,
                                color = Color.Gray,
                                fontSize = 9.sp,
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 2
                            )
                            Spacer(Modifier.height(4.dp))
                        } else {
                            Spacer(Modifier.height(4.dp))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Button(
                                onClick = handleStartClick,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (!isTrackingStarted) GreenButton else DisabledButton,
                                    contentColor = if (!isTrackingStarted) Color.White else Color.Gray,
                                    disabledContainerColor = DisabledButton,
                                    disabledContentColor = Color.Gray
                                ),
                                contentPadding = PaddingValues(vertical = 8.dp),
                                enabled = !isTrackingStarted
                            ) {
                                Text(
                                    "Start",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (!isTrackingStarted) Color.White else Color.Gray
                                )
                            }
                            Button(
                                onClick = handleStopClick,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isTrackingStarted) RedButton else DisabledButton,
                                    contentColor = if (isTrackingStarted) Color.White else Color.Gray,
                                    disabledContainerColor = DisabledButton,
                                    disabledContentColor = Color.Gray
                                ),
                                contentPadding = PaddingValues(vertical = 8.dp),
                                enabled = isTrackingStarted
                            ) {
                                Text(
                                    "Stop",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isTrackingStarted) Color.White else Color.Gray
                                )
                            }
                        }
                    }

                    Spacer(Modifier.width(4.dp))

                    // Right Section - Fare and Controls
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "FARE $",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = String.format("%.2f", totalFare),
                            color = RedDigital,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )

                        Spacer(Modifier.height(6.dp))

                        Text(
                            text = "EXTRAS $",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                        Text(
                            text = String.format("%.2f", extras.value),
                            color = RedDigital,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Button(
                                onClick = { extras.value = (extras.value + 0.10).coerceAtMost(99.99) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = DarkGrey),
                                contentPadding = PaddingValues(vertical = 3.dp)
                            ) {
                                Text("+", fontSize = 11.sp, color = Color.Gray)
                            }
                            Button(
                                onClick = { extras.value = (extras.value - 0.10).coerceAtLeast(0.0) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = DarkGrey),
                                contentPadding = PaddingValues(vertical = 3.dp)
                            ) {
                                Text("-", fontSize = 11.sp, color = Color.Gray)
                            }
                        }

                        Spacer(Modifier.height(6.dp))

                        Text(
                            text = "TOLLS $",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                        Text(
                            text = String.format("%.2f", tolls.value),
                            color = RedDigital,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                        Button(
                            onClick = {},
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkGrey),
                            contentPadding = PaddingValues(vertical = 3.dp)
                        ) {
                            Text("1 SET", fontSize = 9.sp, color = Color.Gray)
                        }
                    }
                }
        }
    }
}

@SuppressLint("DefaultLocale")
private fun formatDuration(seconds: Long): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return String.format("%d:%02d", minutes, secs)
}
