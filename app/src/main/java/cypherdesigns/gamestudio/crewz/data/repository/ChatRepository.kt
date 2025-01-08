package cypherdesigns.gamestudio.crewz.data.repository

import android.content.Context
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import cypherdesigns.gamestudio.crewz.ui.chat.Message
import cypherdesigns.gamestudio.crewz.ui.chat.Chatroom

class ChatRepository {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val chatroomsRef: DatabaseReference = database.getReference("chatrooms")

    fun getChatrooms(onResult: (List<Chatroom>) -> Unit) {
        chatroomsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val chatrooms = mutableListOf<Chatroom>()

                val pendingUpdates = snapshot.children.count()

                if (pendingUpdates == 0) {
                    // No chatrooms available
                    onResult(emptyList())
                    return
                }

                snapshot.children.forEach { child ->
                    val chatroom = child.getValue(Chatroom::class.java)
                    if (chatroom != null) {
                        val chatroomId = child.key ?: return@forEach

                        // Fetch the last message for each chatroom
                        chatroomsRef.child(chatroomId).child("messages")
                            .orderByChild("timestamp")
                            .limitToLast(1)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(messageSnapshot: DataSnapshot) {
                                    if (messageSnapshot.exists()) {
                                        val lastMessageData = messageSnapshot.children.firstOrNull()
                                        if (lastMessageData != null) {
                                            val lastMessage = lastMessageData.child("text")
                                                .getValue(String::class.java) ?: ""
                                            val lastMessageTimestamp =
                                                lastMessageData.child("timestamp")
                                                    .getValue(Long::class.java) ?: 0L
                                            chatroom.lastMessage = lastMessage
                                            chatroom.lastMessageTimeStamp = lastMessageTimestamp
                                        }
                                    }

                                    // Add updated chatroom to the list
                                    chatrooms.add(chatroom)

                                    // Once all updates are complete, trigger the callback
                                    if (chatrooms.size == pendingUpdates) {
                                        onResult(chatrooms)
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    println("Failed to fetch last message for chatroom $chatroomId: ${error.message}")
                                    chatrooms.add(chatroom)

                                    // Ensure callback even on failure
                                    if (chatrooms.size == pendingUpdates) {
                                        onResult(chatrooms)
                                    }
                                }
                            })
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                println("Failed to fetch chatrooms: ${error.message}")
                onResult(emptyList())
            }
        })
    }


    private fun updateLastMessage(chatroomId: String, lastMessage: String) {
        val databaseReference =
            FirebaseDatabase.getInstance().getReference("chatrooms").child(chatroomId)
        val updates = mapOf(
            "lastMessage" to lastMessage,
            "lastMessageTimestamp" to System.currentTimeMillis()
        )
        databaseReference.updateChildren(updates)
            .addOnSuccessListener {
                println("Last Messages updated successfully")
            }
            .addOnFailureListener {
                println("Failed to update last messages: ${it.message}")
            }
    }

    fun getMessages(chatroomId: String, onResult: (List<Message>) -> Unit) {
        chatroomsRef.child(chatroomId).child("messages")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val messages =
                        snapshot.children.mapNotNull { it.getValue(Message::class.java) }
                    onResult(messages)
                }

                override fun onCancelled(error: DatabaseError) {
                    println("Error fetching messages: ${error.message}")
                }

            })
    }

    fun sendMessage(chatroomId: String, message: Message, onComplete: (Boolean) -> Unit) {
        chatroomsRef.child(chatroomId).child("messages").push().setValue(message)
            .addOnCompleteListener { task -> onComplete(task.isSuccessful) }
        updateLastMessage(chatroomId, message.text)
    }

    fun createChatroom(
        chatroom: Chatroom,
        context: Context,
        onComplete: (Boolean) -> Unit
    ) {
        println("ChatRepository: Attempting to create chatroom with id: ${chatroom.id}, name: ${chatroom.name}")

        chatroomsRef.child(chatroom.id).setValue(chatroom)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        context,
                        "Chatroom created successfully: ${chatroom.name}",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        context,
                        "Failed to create chatroom: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                onComplete(task.isSuccessful)
            }
    }
}