package com.example.locationtrackingappv2.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.locationtrackingappv2.presentation.viewmodel.TrackingViewModel
import java.text.SimpleDateFormat
import java.util.*

val PanelGrey = Color(0xFF101820)
val SoftGrey = Color(0xFF1A242E)
val RedDigital = Color(0xFFFF3333)

@Composable
fun TrackingFareScreen(viewModel: TrackingViewModel) {

    val stats by viewModel.stats.collectAsState()
    val fare by viewModel.fare.collectAsState()
    val remaining by viewModel.remainingDistance.collectAsState()
    val routeDistance by viewModel.routeDistance.collectAsState()
    val meterRunning by viewModel.meterRunning.collectAsState()

    // Distance + Duration
    val km = (stats?.totalDistanceMeters ?: 0.0) / 1000.0
    val durationSec = 0
    val durationText = formatDuration(durationSec)

    // Fare Components
    var extras by remember { mutableStateOf(0.10) }
    var tolls by remember { mutableStateOf(1.30) }

    // Meter On Time
    val startTime = System.currentTimeMillis() - (durationSec * 1000)
    val dateFormat = SimpleDateFormat("MMM dd - HH:mm", Locale.getDefault())

    Column(
        Modifier
            .fillMaxSize()
            .background(SoftGrey)
            .padding(10.dp)
    ) {

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {

            // ======================================
            // LEFT PANEL (TRIP DETAILS BOX)
            // ======================================
            Column(
                modifier = Modifier
                    .weight(1.3f)
                    .background(PanelGrey, RoundedCornerShape(6.dp))
                    .padding(12.dp)
            ) {

                InfoText("On Trip NO :", "3")
                InfoText("Booking NO :", "14511")
                InfoText("Requested :", dateFormat.format(Date(startTime)))
                InfoText("From :", "SoftClient")
                InfoText("Meter On :", dateFormat.format(Date(startTime)))
                InfoText("Duration :", "$durationText")
                InfoText("Distance :", "%.2f km".format(km))

                Spacer(Modifier.height(6.dp))
                Divider(color = Color.Gray, thickness = 1.dp)
                Spacer(Modifier.height(6.dp))

                // Pickup Card
                Box(
                    Modifier
                        .background(Color(0xFF263543), RoundedCornerShape(6.dp))
                        .padding(10.dp)
                        .fillMaxWidth()
                ) {
                    Column {
                        Text("1    Pick Up", color = Color.White, fontSize = 12.sp)
                        Text(
                            "10, Waterfall Cres, Bella Vista NSW 2153, Australia",
                            color = Color.LightGray,
                            fontSize = 10.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Button(
                            onClick = { /* Navigation */ },
                            modifier = Modifier.align(Alignment.End),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF122D55)),
                            contentPadding = PaddingValues(4.dp)
                        ) {
                            Text("Nav", color = Color.White, fontSize = 10.sp)
                        }
                    }
                }

                Spacer(Modifier.height(6.dp))

                Button(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                ) {
                    Text("Cancel", color = Color.White)
                }
            }

            // ======================================
            // RIGHT PANEL (FARE, EXTRAS, TOLLS)
            // ======================================
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {

                // --- FARE DIGITAL ---
                Text(
                    "FARE $",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    String.format("%.2f", fare + extras + tolls),
                    fontSize = 38.sp,
                    color = RedDigital,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(Modifier.height(10.dp))

                // --- EXTRAS ---
                Text("EXTRAS $", color = Color.White, fontSize = 12.sp)
                Text(
                    String.format("%.2f", extras),
                    fontSize = 22.sp,
                    color = RedDigital,
                    fontFamily = FontFamily.Monospace
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    MeterSmallBtn("+") { extras += 0.10 }
                    MeterSmallBtn("-") { extras = (extras - 0.10).coerceAtLeast(0.0) }
                }

                Spacer(Modifier.height(10.dp))

                // --- TOLLS ---
                Text("TOLLS $", color = Color.White, fontSize = 12.sp)
                Text(
                    String.format("%.2f", tolls),
                    fontSize = 22.sp,
                    color = RedDigital,
                    fontFamily = FontFamily.Monospace
                )

                Button(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = PanelGrey)
                ) { Text("1 SET", color = Color.White, fontSize = 12.sp) }

                Spacer(Modifier.height(20.dp))

                // Tools Details Arrow
                Text("Tools Details ▾", color = Color.Gray, fontSize = 11.sp)
            }
        }

        Spacer(Modifier.height(12.dp))

        // ======================================
        // BOTTOM BAR: PAUSE / WAIT / METER
        // ======================================
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            BottomBarButton("Pause")
            BottomBarButton("Wait")
            BottomBarButton("Meter")
        }
    }
}

@Composable
fun InfoText(label: String, value: String) {
    Row {
        Text(label, color = Color.LightGray, fontSize = 12.sp)
        Text(value, color = Color.White, fontSize = 12.sp)
    }
}

@Composable
fun MeterSmallBtn(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = PanelGrey),
        contentPadding = PaddingValues(0.dp),
        modifier = Modifier.size(35.dp)
    ) {
        Text(text, color = Color.White)
    }
}

@Composable
fun BottomBarButton(text: String) {
    Button(
        onClick = {},
        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
        modifier = Modifier.width(90.dp)
    ) {
        Text(text, color = Color.White)
    }
}

fun formatDuration(sec: Int): String {
    val m = sec / 60
    val s = sec % 60
    return "%02d:%02d".format(m, s)
}
