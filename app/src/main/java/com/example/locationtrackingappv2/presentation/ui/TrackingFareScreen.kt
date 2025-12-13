package com.example.locationtrackingappv2.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.locationtrackingappv2.presentation.viewmodel.TrackingViewModel
import com.example.locationtrackingappv2.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

   // visible circle


@Composable
fun TrackingFareScreen(viewModel: TrackingViewModel) {

    val stats by viewModel.stats.collectAsState()
    val fare by viewModel.fare.collectAsState()
    val remaining by viewModel.remainingDistance.collectAsState()
    val routeDistance by viewModel.routeDistance.collectAsState()
    val meterRunning by viewModel.meterRunning.collectAsState()

    val distanceKm = (stats?.totalDistanceMeters ?: 0.0) / 1000.0
    val durationSec = stats?.durationSeconds ?: 0
    val durationText = formatDuration(durationSec)


    var extras by remember { mutableStateOf(0.0) }
    var tolls by remember { mutableStateOf(0.00) }

    val startTime = System.currentTimeMillis() - (durationSec * 1000)
    val dateFormat = SimpleDateFormat("MMM dd - HH:mm", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftGrey)
            .verticalScroll(rememberScrollState())
            .padding(10.dp)
    ) {

        // ==========================================================
        // 1. TOP: TRIP DETAILS PANEL
        // ==========================================================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(PanelGrey, shape = RoundedCornerShape(10.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp)

        ) {

            InfoText("On Trip NO :", "3")
            InfoText("Booking NO :", "14511")
            InfoText("Requested :", dateFormat.format(Date(startTime)))
            InfoText("From :", "SoftClient")
            InfoText("Meter On :", dateFormat.format(Date(startTime)))
            InfoText("Duration :", durationText)
            InfoText("Distance :", "%.2f km".format(distanceKm))

            Spacer(Modifier.height(8.dp))
            Divider(color = Color.Gray, thickness = 1.dp)
            Spacer(Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF223242), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Column {
                    Text("1  Pick Up", color = Color.White, fontSize = 13.sp)
                    Text(
                        "10, Waterfall Cres, Bella Vista NSW 2153, Australia",
                        color = TextPrimary,
                        fontSize = 10.sp
                    )
                    Spacer(Modifier.height(5.dp))
                    Button(
                        onClick = {},
                        modifier = Modifier.align(Alignment.End),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1B3254)
                        )
                    ) {
                        Text("Nav", color = Color.White, fontSize = 11.sp)
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            Button(
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
            ) {
                Text("Cancel", color = Color.White)
            }
        }

        Spacer(Modifier.height(20.dp))

        // ==========================================================
        // 2. FARE PANEL (Digital)
        // ==========================================================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(PanelGrey, RoundedCornerShape(10.dp))
                .padding(15.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text("FARE $", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)

            Text(
                text = String.format("%.2f", fare + extras + tolls),
                color = RedDigital,
                fontFamily = FontFamily.Monospace,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(20.dp))

            // Extras
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("EXTRAS $", color = Color.White, fontSize = 12.sp)
                    Text(
                        text = String.format("%.2f", extras),
                        fontSize = 22.sp,
                        color = RedDigital,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    MeterCircleButton("+") { extras += 0.0 }
                    MeterCircleButton("-") { extras = (extras - 0.0).coerceAtLeast(0.0) }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Tolls
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("TOLLS $", color = Color.White, fontSize = 12.sp)
                    Text(
                        text = String.format("%.2f", tolls),
                        fontSize = 22.sp,
                        fontFamily = FontFamily.Monospace,
                        color = RedDigital
                    )
                }

                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(containerColor = SoftGrey),
                    modifier = Modifier.width(90.dp)
                ) { Text("1 SET", fontSize = 12.sp, color = Color.White) }
            }

            Spacer(Modifier.height(10.dp))
            Text("Tools Details ▾", color = Color.Gray, fontSize = 12.sp)
        }

        Spacer(Modifier.height(20.dp))

        // ==========================================================
        // 3. BOTTOM BUTTONS (Always Fixed)
        // ==========================================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {

            // START METER BUTTON
            Button(
                onClick = { viewModel.startMeter() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!viewModel.meterRunning.collectAsState().value) Color(0xFF2E7D32) else Color.DarkGray
                ),
                enabled = !viewModel.meterRunning.collectAsState().value,
                modifier = Modifier.width(120.dp)
            ) {
                Text("START", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }

            // STOP METER BUTTON
            Button(
                onClick = { viewModel.stopMeter() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (viewModel.meterRunning.collectAsState().value) Color(0xFFC62828) else Color.DarkGray
                ),
                enabled = viewModel.meterRunning.collectAsState().value,
                modifier = Modifier.width(120.dp)
            ) {
                Text("STOP", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }

    }
}


@Composable
fun InfoText(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp), // more breathing space
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 14.sp,  // Increased from 12sp → readable
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            color = TextPrimary,
            fontSize = 14.sp,  // Increased from 12sp
            fontWeight = FontWeight.SemiBold
        )
    }
}


@Composable
fun MeterCircleButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .background(ButtonCircle, shape = CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

fun formatDuration(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%02d:%02d".format(m, s)
}
