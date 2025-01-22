package cypherdesigns.gamestudio.crewz.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cypherdesigns.gamestudio.crewz.viewmodel.CrewViewModel
import cypherdesigns.gamestudio.crewz.viewmodel.UserViewModel
import java.util.Locale

@Composable
fun SettingsScreen(
    userViewModel: UserViewModel,
    crewViewModel: CrewViewModel,
    userId: String,
    crewId: String,
    onBack: () -> Unit,
    onMenuClick: () -> Unit
) {
    val isLocationSharingEnabled by crewViewModel.isLocationSharingEnabled.collectAsState()
    val selectedColor by crewViewModel.markerColorName.collectAsState()
    val currentCrewName by crewViewModel.currentCrewName.collectAsState()
    val currentCrewRole by crewViewModel.currentCrewRole.collectAsState()

    LaunchedEffect(Unit) {
        crewViewModel.observeUserDetails(crewId, userId)
        crewViewModel.observeMarkerColorAndLocationSharing(crewId, userId)
        crewViewModel.fetchCrewName(crewId)
        crewViewModel.observeCrewMembers(crewId)
        crewViewModel.observeMemberRole(crewId, userId)
    }

    Scaffold(
        topBar = {
            AppTopAppBar(
                title = "Settings",
                onSettingsClick = onBack,
                onMenuClick = onMenuClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = "Profile", style = androidx.compose.material3.MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(4.dp))

            Text(text = "Username:", style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)
            Text(text = "${userViewModel.cachedUserName}", style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)

            Text(text = "Name:", style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)
            Text(text = "${userViewModel.cachedFirstName} ${userViewModel.cachedLastName}",
                style = androidx.compose.material3.MaterialTheme.typography.headlineSmall
            )

            Spacer(Modifier.height(4.dp))

            Text(text = "Crew:", style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)
            Text(text = currentCrewName ?: "No Crew",
                style = androidx.compose.material3.MaterialTheme.typography.headlineSmall
            )
            Text(text = "Role:", style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)

            val displayRole = currentCrewRole?.replaceFirstChar { it.uppercaseChar() } ?: "No Crew"
            Text(text = displayRole,
                style = androidx.compose.material3.MaterialTheme.typography.headlineSmall
            )
            Spacer(Modifier.height(4.dp))

            Text(text = "Email:", style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)
            Text(text = "${userViewModel.cachedEmail}",
                style = androidx.compose.material3.MaterialTheme.typography.headlineSmall
            )

            Spacer(Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Location Sharing", style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)
                Switch(
                    checked = isLocationSharingEnabled,
                    onCheckedChange = { enabled ->
                        crewViewModel.toggleLocationSharing(crewId, userId, enabled)
                    }
                )
            }

            Text("Marker Color:", style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)

            // Our color picker
            ColorPicker(
                currentColor = selectedColor,
                onColorSelected = { colorName ->
                    crewViewModel.updateMarkerColor(crewId, userId, colorName)
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

    androidx.compose.foundation.lazy.LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(colors.size) { index ->
            val colorName = colors[index]
            val colorRes = when (colorName) {
                "red" -> androidx.compose.ui.graphics.Color.Red
                "orange" -> androidx.compose.ui.graphics.Color(0xFFFFA500)
                "yellow" -> androidx.compose.ui.graphics.Color.Yellow
                "green" -> androidx.compose.ui.graphics.Color.Green
                "cyan" -> androidx.compose.ui.graphics.Color.Cyan
                "blue" -> androidx.compose.ui.graphics.Color.Blue
                "purple" -> androidx.compose.ui.graphics.Color(0xFF800080)
                "magenta" -> androidx.compose.ui.graphics.Color.Magenta
                "white" -> androidx.compose.ui.graphics.Color.White
                "gray" -> androidx.compose.ui.graphics.Color.Gray
                "black" -> androidx.compose.ui.graphics.Color.Black
                else -> androidx.compose.ui.graphics.Color.Red
            }
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(colorRes, shape = androidx.compose.foundation.shape.CircleShape)
                    .border(
                        width = 2.dp,
                        color = if (colorName == currentColor) androidx.compose.ui.graphics.Color.Black
                        else androidx.compose.ui.graphics.Color.Transparent,
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
                    .clickable {
                        onColorSelected(colorName)
                    }
            )
        }
    }
}
