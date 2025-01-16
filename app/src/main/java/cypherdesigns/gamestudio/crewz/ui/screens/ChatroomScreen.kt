package cypherdesigns.gamestudio.crewz.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import cypherdesigns.gamestudio.crewz.R
import cypherdesigns.gamestudio.crewz.ui.chat.Message
import cypherdesigns.gamestudio.crewz.ui.chat.formatTimestamp
import cypherdesigns.gamestudio.crewz.viewmodel.ChatViewModel
import cypherdesigns.gamestudio.crewz.viewmodel.UserViewModel

@Composable
fun ChatroomScreen(
    viewModel: ChatViewModel,
    userViewModel: UserViewModel,
    crewId: String,
    chatroomId: String,
    onSettingsClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    val messages by viewModel.messages.collectAsState()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val messageText = remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.fetchMessages(crewId, chatroomId)
        userViewModel.observeCrewMembers(crewId)
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
        Column(modifier = Modifier.fillMaxSize()) {
            // Display chat messages
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(messages) { message ->
                    val isSentByCurrentUser = message.senderId == currentUserId

                    Row(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth()
                            .padding(innerPadding),
                        horizontalArrangement = if (isSentByCurrentUser) Arrangement.End else Arrangement.Start
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .padding(
                                        start = if (isSentByCurrentUser) 48.dp else 8.dp, // More whitespace on the start for others
                                        end = if (isSentByCurrentUser) 8.dp else 48.dp  // More whitespace on the end for current user
                                    )
                                    .background(
                                        color = if (isSentByCurrentUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                        shape = MaterialTheme.shapes.medium
                                    )
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Text(
                                        text = if (isSentByCurrentUser) "You:" else "${message.senderFirstName}:",
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

            // Input row for composing messages
            Row(modifier = Modifier.padding(16.dp)) {
                TextField(
                    value = messageText.value,
                    onValueChange = { messageText.value = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message...") }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        val currentUser = FirebaseAuth.getInstance().currentUser
                        val senderId = currentUser?.uid ?: "Unknown"

                        FirebaseDatabase.getInstance().getReference("users")
                            .child(senderId)
                            .get()
                            .addOnSuccessListener { dataSnapshot ->
                                val senderFirstName =
                                    dataSnapshot.child("firstName").getValue(String::class.java)
                                        ?: "Unknown"
                                val message = Message(
                                    senderFirstName = senderFirstName,
                                    senderId = senderId,
                                    text = messageText.value,
                                    timestamp = System.currentTimeMillis()
                                )
                                viewModel.sendMessage(crewId, chatroomId, message)
                                messageText.value = ""
                            }
                            .addOnFailureListener {
                                println("Failed to fetch sender's first name: ${it.message}")
                            }
                    }
                ) {
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