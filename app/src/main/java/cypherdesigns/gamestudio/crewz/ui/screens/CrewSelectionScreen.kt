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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
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
    val database = FirebaseDatabase.getInstance().getReference("crews")

    var crews by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var newCrewName by remember { mutableStateOf("") }
    var showCreateCrewDialog by remember { mutableStateOf(false) }

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
        LazyColumn(modifier = Modifier.padding(paddingValues)) {
            items(crews) { (crewId, name) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clickable {
                            onCrewSelected(crewId)
                        }
                ) {
                    Text(
                        text = name,
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
                                    "ownerId" to currentUserId
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
