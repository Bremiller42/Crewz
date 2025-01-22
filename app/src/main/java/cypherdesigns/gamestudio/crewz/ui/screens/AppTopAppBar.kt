package cypherdesigns.gamestudio.crewz.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import cypherdesigns.gamestudio.crewz.R
import cypherdesigns.gamestudio.crewz.ui.menus.SettingsDropdownMenu
import cypherdesigns.gamestudio.crewz.viewmodel.CrewViewModel

@Composable
fun AppTopAppBar(
    title: String,
    crewViewModel: CrewViewModel,
    onAccountSettings: () -> Unit,
    onCrewSettings: () -> Unit,
    onLogout: () -> Unit,
    onMenuClick: () -> Unit,
    onBack: () -> Unit
) {
    // State for dropdown menu visibility
    var isDropdownExpanded by remember { mutableStateOf(false) }

    androidx.compose.material.TopAppBar(
        backgroundColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.primary
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            // Title at the center
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )

            // Menu button (left-aligned)
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_members_menu),
                    contentDescription = "Menu",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Settings or Back button (right-aligned)
            IconButton(
                onClick = {
                    if (title == "Settings") {
                        onBack() // Call onBack when the title is "Settings"
                    } else {
                        isDropdownExpanded = !isDropdownExpanded
                    }
                },
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                if (title != "Settings") {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_settings),
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Dropdown menu for settings
            if (isDropdownExpanded) {
                Box(modifier = Modifier.align(Alignment.TopEnd)) {
                    SettingsDropdownMenu(
                        crewViewModel = crewViewModel,
                        onDismiss = { isDropdownExpanded = false },
                        onAccountSettings = {
                            isDropdownExpanded = false
                            onAccountSettings()
                        },
                        onCrewSettings = {
                            isDropdownExpanded = false
                            onCrewSettings()
                        },
                        onLogout = {
                            isDropdownExpanded = false
                            onLogout()
                        }
                    )
                }
            }
        }
    }
}
