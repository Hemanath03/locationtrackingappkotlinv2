package com.example.locationtrackingappv2.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.locationtrackingappv2.presentation.viewmodel.TrackingViewModel

// -----------------------------------------------------
// MAIN SCREEN
// -----------------------------------------------------
@Composable
fun TrackingMainScreen(
    viewModel: TrackingViewModel
) {
    var selectedTab by remember { mutableStateOf(TrackingTab.MAP) }

    Scaffold(
        bottomBar = {
            UberBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(bottom = padding.calculateBottomPadding())
                .fillMaxSize()
        ) {
            when (selectedTab) {
                TrackingTab.MAP -> TrackingMapScreen(viewModel)
                TrackingTab.FARE -> TrackingFareScreen(viewModel)
            }
        }
    }
}

// -----------------------------------------------------
// UBER STYLE BOTTOM BAR
// -----------------------------------------------------
@Composable
fun UberBottomBar(
    selectedTab: TrackingTab,
    onTabSelected: (TrackingTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(Color.Black),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // MAP TAB
        UberBottomTab(
            modifier = Modifier.weight(1f),
            selected = selectedTab == TrackingTab.MAP,
            icon = Icons.Default.LocationOn,
            label = "Map",
            onClick = { onTabSelected(TrackingTab.MAP) }
        )

        // FARE TAB
        UberBottomTab(
            modifier = Modifier.weight(1f),
            selected = selectedTab == TrackingTab.FARE,
            icon = Icons.Default.Info,
            label = "Fare",
            onClick = { onTabSelected(TrackingTab.FARE) }
        )
    }
}

// -----------------------------------------------------
// UBER TAB ITEM (FULL BACKGROUND HIGHLIGHT)
// -----------------------------------------------------
@Composable
fun UberBottomTab(
    modifier: Modifier,
    selected: Boolean,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val bg = if (selected) Color(0xFF1F2A33) else Color.Black
    val tint = if (selected) Color.White else Color(0xFF8A8F94)

    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(bg)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Icon(
                icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(22.dp)
            )

            Text(
                label,
                color = tint,
                fontSize = 12.sp
            )
        }
    }
}


