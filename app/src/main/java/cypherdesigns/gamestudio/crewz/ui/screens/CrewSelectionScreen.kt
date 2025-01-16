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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import cypherdesigns.gamestudio.crewz.R
import cypherdesigns.gamestudio.crewz.viewmodel.UserViewModel

@Composable
fun CrewSelectionScreen(
    userViewModel: UserViewModel,
    onCrewSelected: (String) -> Unit,
    onSettingsClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    val context = LocalContext.current
    val database = FirebaseDatabase.getInstance().getReference("crews")

    // State to hold the list of crews
    var crews by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }

    // State for the new crew name
    var newCrewName by remember { mutableStateOf("") }
    var showCreateCrewDialog by remember { mutableStateOf(false) } // Controls dialog visibility

    // Fetch crews from Firebase
    DisposableEffect(Unit) {
        val crewsListener = database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                crews = snapshot.children.mapNotNull { crew ->
                    val crewId = crew.key
                    val name = crew.child("name").getValue(String::class.java)
                    if (crewId != null && name != null) Pair(crewId, name) else null
                }
            }

            override fun onCancelled(error: DatabaseError) {
                println("Failed to fetch crews: ${error.message}")
            }
        })

        onDispose {
            database.removeEventListener(crewsListener)
        }
    }

    // Scaffold to hold the crew list and FAB
    Scaffold(
        topBar = { AppTopAppBar(title = "Join a Crew", onSettingsClick = onSettingsClick, onMenuClick = onMenuClick) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateCrewDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_upload), // Replace with your icon resource
                    contentDescription = "Create Crew",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { paddingValues ->
        // LazyColumn to display the list of crews
        LazyColumn(
            modifier = Modifier.padding(paddingValues)
        ) {
            items(crews) { (crewId, name) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clickable {
                            // Update user's selected crewId in Firebase
                            userViewModel.updateCrewId(crewId)
                            onCrewSelected(crewId) // Navigate to the crew's page or map screen
                        }
                ) {
                    // Display crew name
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    // "Join" text as clickable action
                    Text(
                        text = "Join",
                        color = Color.Blue,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // Dialog to create a new crew
        if (showCreateCrewDialog) {
            CreateCrewDialog(
                newCrewName = newCrewName,
                onCrewNameChange = { newCrewName = it },
                onCreateClick = {
                    if (newCrewName.isNotBlank()) {
                        val crewId = database.push().key ?: return@CreateCrewDialog
                        val currentUserId = userViewModel.currentUserId
                        if (currentUserId != null) {
                            database.child(crewId).setValue(
                                mapOf(
                                    "name" to newCrewName,
                                    "ownerId" to currentUserId // Set the current user as the owner
                                )
                            )
                            newCrewName = ""
                            showCreateCrewDialog = false
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
