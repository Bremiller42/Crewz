package cypherdesigns.gamestudio.crewz.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cypherdesigns.gamestudio.crewz.viewmodel.UserViewModel

@Composable
fun SettingsScreen(
    viewModel: UserViewModel,
    userId: String,
    onBack: () -> Unit
) {
    val isLocationSharingEnabled by viewModel.isLocationSharingEnabled.collectAsState()


    LaunchedEffect(Unit) {
        viewModel.observeUserDetails(userId)
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
            Text(
                text = "Name: ${viewModel.cachedFirstName} ${viewModel.cachedLastName}",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "Email: ${viewModel.cachedEmail}",
                style = MaterialTheme.typography.bodyLarge
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Location Sharing",
                    style = MaterialTheme.typography.bodyLarge
                )
                androidx.compose.material3.Switch(
                    checked = isLocationSharingEnabled,
                    onCheckedChange = { isEnabled ->
                        viewModel.toggleLocationSharing(userId, isEnabled)
                    }
                )
            }
        }
    }
}