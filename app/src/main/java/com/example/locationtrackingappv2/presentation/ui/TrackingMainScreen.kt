import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.locationtrackingappv2.presentation.ui.TrackingFareScreen
import com.example.locationtrackingappv2.presentation.ui.TrackingMapScreen
import com.example.locationtrackingappv2.presentation.ui.TrackingTab
import com.example.locationtrackingappv2.presentation.viewmodel.TrackingViewModel

@Composable
fun TrackingMainScreen(
    viewModel: TrackingViewModel
) {
    var selectedTab by remember { mutableStateOf(TrackingTab.MAP) }

    Scaffold(
        bottomBar = {
            NavigationBar {

                NavigationBarItem(
                    selected = selectedTab == TrackingTab.MAP,
                    onClick = { selectedTab = TrackingTab.MAP },
                    icon = { Icon(Icons.Default.Place, contentDescription = "Map") },
                    label = { Text("Map") }
                )

                NavigationBarItem(
                    selected = selectedTab == TrackingTab.FARE,
                    onClick = { selectedTab = TrackingTab.FARE },
                    icon = { Icon(Icons.Default.Star, contentDescription = "Fare") },
                    label = { Text("Fare") }
                )
            }
        }
    ) { padding ->

        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                TrackingTab.MAP -> TrackingMapScreen(viewModel)
                TrackingTab.FARE -> TrackingFareScreen(viewModel)
            }
        }
    }
}
