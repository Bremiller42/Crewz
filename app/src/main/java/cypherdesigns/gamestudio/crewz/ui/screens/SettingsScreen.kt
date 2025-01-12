package cypherdesigns.gamestudio.crewz.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
    userId: String,
    onBack: () -> Unit
) {
    val isLocationSharingEnabled by viewModel.isLocationSharingEnabled.collectAsState()
    val selectedHue by viewModel.markerColorHue.collectAsState()


    LaunchedEffect(Unit) {
        viewModel.observeUserDetails(userId)
        viewModel.observeMarkerColor(userId)
    }

    Scaffold(
        topBar = {
            AppTopAppBar(title = "Settings", onSettingsClick = onBack)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                        viewModel.toggleLocationSharing(userId, isEnabled)
                    }
                )
            }
            // Color Picker with Slider
            Text("Marker Color:")
            ColorPicker(
                selectedHue = selectedHue,
                onColorSelected = { hue ->
                    viewModel.updateMarkerColor(userId, hue)
                }
            )
        }
    }
}

@Composable
fun ColorPicker(
    selectedHue: Float,
    onColorSelected: (Float) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Display the current selected color
        val selectedColor = Color.hsv(selectedHue, 1f, 1f)
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(selectedColor, androidx.compose.foundation.shape.CircleShape)
        )

        Spacer(Modifier.height(8.dp))

        // Hue Slider
        androidx.compose.material3.Slider(
            value = selectedHue,
            onValueChange = { hue -> onColorSelected(hue) },
            valueRange = 0f..360f,
            colors = androidx.compose.material3.SliderDefaults.colors(
                thumbColor = selectedColor,
                activeTrackColor = selectedColor,
                inactiveTrackColor = selectedColor.copy(alpha = 0.3f)
            )
        )

        Text(
            text = "Hue: ${selectedHue.toInt()}°",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
