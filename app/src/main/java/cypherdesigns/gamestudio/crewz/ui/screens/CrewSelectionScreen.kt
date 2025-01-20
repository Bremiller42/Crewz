package cypherdesigns.gamestudio.crewz.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import cypherdesigns.gamestudio.crewz.R
import cypherdesigns.gamestudio.crewz.viewmodel.CrewViewModel
import cypherdesigns.gamestudio.crewz.viewmodel.UserViewModel

@Composable
fun CrewSelectionScreen(
    userViewModel: UserViewModel,
    crewViewModel: CrewViewModel,
    onCrewSelected: (String) -> Unit,
    onSettingsClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    val context = LocalContext.current

    // Observe a list of all crews from the CrewViewModel
    val crews by crewViewModel.availableCrews.collectAsState()

    // Local state for the new crew name & dialog
    var newCrewName by remember { mutableStateOf("") }
    var showCreateCrewDialog by remember { mutableStateOf(false) }

    // 1) Start observing all crews when this composable appears
    LaunchedEffect(Unit) {
        crewViewModel.observeAllCrews()
    }

    Scaffold(
        topBar = {
            AppTopAppBar(
                title = "Join a Crew",
                onSettingsClick = onSettingsClick,
                onMenuClick = onMenuClick
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateCrewDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_upload),
                    contentDescription = "Create Crew",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { paddingValues ->
        // 2) Display the list of existing crews
        LazyColumn(modifier = Modifier.padding(paddingValues)) {
            items(crews) { crewInfo ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clickable {
                            // When user clicks a crew, we pass that ID upstream
                            onCrewSelected(crewInfo.id)
                        }
                ) {
                    Text(
                        text = crewInfo.name,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "Join",
                        color = Color.Blue,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // 3) If user clicks FAB, show a dialog to create a new crew
        if (showCreateCrewDialog) {
            CreateCrewDialog(
                newCrewName = newCrewName,
                onCrewNameChange = { newCrewName = it },
                onCreateClick = {
                    if (newCrewName.isNotBlank()) {
                        val currentUserId = userViewModel.currentUserId
                        if (currentUserId != null) {
                            crewViewModel.createCrew(
                                crewName = newCrewName,
                                ownerUserId = currentUserId,
                                onSuccess = { newCrewId ->
                                    // Crew created successfully
                                    newCrewName = ""
                                    showCreateCrewDialog = false
                                },
                                onFailure = { error ->
                                    Toast.makeText(
                                        context,
                                        "Failed to create crew: $error",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )
                        } else {
                            Toast.makeText(
                                context,
                                "User ID not found. Unable to create crew.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(context, "Crew name cannot be empty", Toast.LENGTH_SHORT)
                            .show()
                    }
                },
                onCancelClick = {
                    showCreateCrewDialog = false
                }
            )
        }
    }
}

@Composable
fun CreateCrewDialog(
    newCrewName: String,
    onCrewNameChange: (String) -> Unit,
    onCreateClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = { onCancelClick() },
        title = { Text(text = "Create New Crew") },
        text = {
            androidx.compose.material3.TextField(
                value = newCrewName,
                onValueChange = onCrewNameChange,
                label = { Text("Crew Name") }
            )
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = onCreateClick) {
                Text("Create")
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onCancelClick) {
                Text("Cancel")
            }
        }
    )
}
