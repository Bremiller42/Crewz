package cypherdesigns.gamestudio.crewz.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import cypherdesigns.gamestudio.crewz.R
import cypherdesigns.gamestudio.crewz.ui.chat.formatTimestamp
import cypherdesigns.gamestudio.crewz.viewmodel.ChatViewModel
import cypherdesigns.gamestudio.crewz.viewmodel.CrewViewModel
import cypherdesigns.gamestudio.crewz.viewmodel.UserViewModel

@Composable
fun ChatroomListScreen(
    viewModel: ChatViewModel,
    crewViewModel: CrewViewModel,
    userViewModel: UserViewModel,
    crewId: String,
    onChatroomSelected: (String) -> Unit,
    onSettingsClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    val chatrooms by viewModel.chatrooms.collectAsState()
    val context = LocalContext.current

    var showDialog by remember { mutableStateOf(false) }
    var newChatroomName by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.fetchChatrooms(crewId) // from ChatViewModel
        crewViewModel.observeCrewMembers(crewId) // watch crew members
    }

    Scaffold(
        topBar = {
            AppTopAppBar(
                title = "Crew Chatrooms",
                onSettingsClick = onSettingsClick,
                onMenuClick = onMenuClick
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_upload),
                    contentDescription = "Create Chatroom",
                    tint = MaterialTheme.colorScheme.background
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            LazyColumn {
                items(chatrooms) { chatroom ->
                    ListItem(
                        headlineContent = {
                            Text(
                                chatroom.name,
                                color = MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.headlineSmall
                            )
                        },
                        supportingContent = {
                            Text(
                                chatroom.lastMessage,
                                color = MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        trailingContent = {
                            Text(
                                formatTimestamp(chatroom.lastMessageTimeStamp),
                                color = MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        modifier = Modifier.clickable {
                            onChatroomSelected(chatroom.id)
                        }
                    )
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = null,
                text = {
                    Column {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("New Chatroom", style = MaterialTheme.typography.bodyMedium)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        TextField(
                            value = newChatroomName,
                            onValueChange = {
                                newChatroomName = it.replaceFirstChar { char ->
                                    if (char.isLowerCase()) char.titlecase() else char.toString()
                                }
                            },
                            label = { Text("Chatroom Name") }
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newChatroomName.isNotBlank()) {
                                viewModel.createChatroom(crewId, newChatroomName, context)
                                newChatroomName = ""
                                showDialog = false
                            } else {
                                Toast.makeText(
                                    context,
                                    "Chatroom name cannot be blank",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    ) {
                        Text("Create")
                    }
                },
                dismissButton = {
                    Button(onClick = { showDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
