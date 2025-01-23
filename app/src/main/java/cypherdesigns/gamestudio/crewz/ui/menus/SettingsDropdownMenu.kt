package cypherdesigns.gamestudio.crewz.ui.menus

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import cypherdesigns.gamestudio.crewz.viewmodel.CrewViewModel

@Composable
fun SettingsDropdownMenu(
    crewViewModel: CrewViewModel,
    onDismiss: () -> Unit,
    onAccountSettings: () -> Unit,
    onCrewSettings: () -> Unit,
    onLogout: () -> Unit
) {
    val currentRole by crewViewModel.currentCrewRole.collectAsState()
    var menuOffset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .onGloballyPositioned { layoutCoordinates ->
                // Capture the global position of the anchor (Settings icon)
                menuOffset = layoutCoordinates.localToWindow(Offset.Zero)
            }
    ) {
        DropdownMenu(
            expanded = true,
            onDismissRequest = onDismiss
        ) {
            // Account Settings
            DropdownMenuItem(
                text = { Text("Account Settings") },
                onClick = onAccountSettings
            )
            // Crew Settings
            if (currentRole in listOf("owner", "admin")) {
                DropdownMenuItem(
                    text = { Text("Crew Settings") },
                    onClick = onCrewSettings
                )
            }
            // Logout
            DropdownMenuItem(
                text = { Text("Logout") },
                onClick = onLogout
            )
        }
    }
}