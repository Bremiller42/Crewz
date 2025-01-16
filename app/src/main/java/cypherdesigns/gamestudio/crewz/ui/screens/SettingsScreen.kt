package cypherdesigns.gamestudio.crewz.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cypherdesigns.gamestudio.crewz.viewmodel.UserViewModel

@Composable
fun SettingsScreen(
    viewModel: UserViewModel,
    crewId: String,
    userId: String,
    onBack: () -> Unit,
    onMenuClick: () -> Unit
) {
    val isLocationSharingEnabled by viewModel.isLocationSharingEnabled.collectAsState()
    val selectedColor by viewModel.markerColorName.collectAsState() // Observe selected color name
    val currentCrewName by viewModel.currentCrewName.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.observeUserDetails(crewId, userId)
        viewModel.observeMarkerColorAndLocationSharing(crewId, userId)
        viewModel.fetchCrewName(crewId)
        viewModel.observeCrewMembers(crewId)
    }

    Scaffold(
        topBar = {
            AppTopAppBar(title = "Settings", onSettingsClick = onBack, onMenuClick = onMenuClick)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                "Profile",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Name:",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "${viewModel.cachedFirstName} ${viewModel.cachedLastName}",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Crew:",
                style = MaterialTheme.typography.headlineSmall
            )
            currentCrewName?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.headlineSmall
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Email:",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "${viewModel.cachedEmail}",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Location Sharing",
                    style = MaterialTheme.typography.headlineSmall
                )
                androidx.compose.material3.Switch(
                    checked = isLocationSharingEnabled,
                    onCheckedChange = { isEnabled ->
                        viewModel.toggleLocationSharing(crewId, userId, isEnabled)
                    }
                )
            }
            // Color Picker with Slider
            Text(
                text = "Marker Color:",
                style = MaterialTheme.typography.headlineSmall
            )

            ColorPicker(
                currentColor = selectedColor,
                onColorSelected = { colorName ->
                    viewModel.updateMarkerColor(crewId, userId, colorName) // Save color selection
                }
            )
        }
    }
}
@Composable
fun ColorPicker(
    currentColor: String,
    onColorSelected: (String) -> Unit
) {
    val colors = listOf(
        "red", "orange", "yellow", "green", "cyan",
        "blue", "purple", "magenta", "white", "gray", "black"
    )

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp), // Add spacing between items
        contentPadding = PaddingValues(horizontal = 16.dp) // Add padding to the start and end
    ) {
        items(colors) { colorName ->
            val colorResId = when (colorName) {
                "red" -> Color.Red
                "orange" -> Color(0xFFFFA500) // Orange
                "yellow" -> Color.Yellow
                "green" -> Color.Green
                "cyan" -> Color.Cyan
                "blue" -> Color.Blue
                "purple" -> Color(0xFF800080) // Purple
                "magenta" -> Color.Magenta
                "white" -> Color.White
                "gray" -> Color.Gray
                "black" -> Color.Black
                else -> Color.Red
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(colorResId, shape = CircleShape)
                    .border(
                        width = 2.dp,
                        color = if (colorName == currentColor) Color.Black else Color.Transparent,
                        shape = CircleShape
                    )
                    .clickable { onColorSelected(colorName) }
            )
        }
    }
}

