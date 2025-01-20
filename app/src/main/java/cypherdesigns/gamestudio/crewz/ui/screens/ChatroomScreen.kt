package cypherdesigns.gamestudio.crewz.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import cypherdesigns.gamestudio.crewz.R
import cypherdesigns.gamestudio.crewz.ui.chat.Message
import cypherdesigns.gamestudio.crewz.ui.chat.formatTimestamp
import cypherdesigns.gamestudio.crewz.viewmodel.ChatViewModel
import cypherdesigns.gamestudio.crewz.viewmodel.CrewViewModel
import cypherdesigns.gamestudio.crewz.viewmodel.UserViewModel
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.TextField
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme

@Composable
fun ChatroomScreen(
    viewModel: ChatViewModel,
    userViewModel: UserViewModel,
    crewViewModel: CrewViewModel,
    crewId: String,
    chatroomId: String,
    onSettingsClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    val messages by viewModel.messages.collectAsState()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val messageText = remember { mutableStateOf("") }

    // Observe the crew members
    LaunchedEffect(Unit) {
        viewModel.fetchMessages(crewId, chatroomId)
        crewViewModel.observeCrewMembers(crewId)
    }

    Scaffold(
        topBar = {
            AppTopAppBar(
                title = "Our Crew",
                onSettingsClick = onSettingsClick,
                onMenuClick = onMenuClick
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)) {

            // Display the chat messages
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(messages) { message ->
                    val isSentByCurrentUser = message.senderId == currentUserId

                    Row(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = if (isSentByCurrentUser) Arrangement.End else Arrangement.Start
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .padding(
                                        start = if (isSentByCurrentUser) 48.dp else 8.dp,
                                        end = if (isSentByCurrentUser) 8.dp else 48.dp
                                    )
                                    .background(
                                        color = if (isSentByCurrentUser)
                                            MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.secondary,
                                        shape = MaterialTheme.shapes.medium
                                    )
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Text(
                                        text = if (isSentByCurrentUser) "You:" else "${message.senderUserName}:",
                                        color = MaterialTheme.colorScheme.background
                                    )
                                    Text(
                                        text = message.text,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.background
                                    )
                                    Text(
                                        text = formatTimestamp(message.timestamp),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.background
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Input row for messages
            Row(modifier = Modifier.padding(16.dp)) {
                TextField(
                    value = messageText.value,
                    onValueChange = { messageText.value = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message...") }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    val senderId = currentUser?.uid ?: "Unknown"

                    FirebaseDatabase.getInstance().getReference("users")
                        .child(senderId)
                        .get()
                        .addOnSuccessListener { dataSnapshot ->
                            val senderUserName =
                                dataSnapshot.child("userName").getValue(String::class.java) ?: "Unknown"
                            val message = Message(
                                senderUserName = senderUserName,
                                senderId = senderId,
                                text = messageText.value,
                                timestamp = System.currentTimeMillis()
                            )
                            viewModel.sendMessage(crewId, chatroomId, message)
                            messageText.value = ""
                        }
                        .addOnFailureListener {
                            println("Failed to fetch sender info: ${it.message}")
                        }
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.send_message),
                        contentDescription = "Send Message",
                        tint = MaterialTheme.colorScheme.background
                    )
                }
            }
        }
    }
}
