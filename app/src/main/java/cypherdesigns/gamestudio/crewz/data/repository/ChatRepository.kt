package cypherdesigns.gamestudio.crewz.data.repository

import android.content.Context
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import cypherdesigns.gamestudio.crewz.ui.chat.Chatroom
import cypherdesigns.gamestudio.crewz.ui.chat.Message

class ChatRepository {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    /**
     * Fetch all messages for a specific chatroom in a crew.
     */
    fun getMessages(crewId: String, chatroomId: String, onResult: (List<Message>) -> Unit) {
        val chatLogsRef =
            database.getReference("crews").child(crewId).child("chatrooms").child(chatroomId)
                .child("messages")
        chatLogsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = snapshot.children.mapNotNull { it.getValue(Message::class.java) }
                onResult(messages)
            }

            override fun onCancelled(error: DatabaseError) {
                println("Error fetching messages for chatroom $chatroomId: ${error.message}")
                onResult(emptyList()) // Return empty list on failure
            }
        })
    }

    /**
     * Send a new message to a specific chatroom in a crew.
     */
    fun sendMessage(
        crewId: String,
        chatroomId: String,
        message: Message,
        onComplete: (Boolean) -> Unit
    ) {
        val chatLogsRef =
            database.getReference("crews").child(crewId).child("chatrooms").child(chatroomId)
                .child("messages")
        chatLogsRef.push().setValue(message)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    updateLastMessage(crewId, chatroomId, message)
                }
                onComplete(task.isSuccessful)
            }
            .addOnFailureListener {
                println("Failed to send message to chatroom $chatroomId: ${it.message}")
                onComplete(false)
            }
    }

    /**
     * Update the last message and its timestamp for a specific chatroom in a crew.
     */
    private fun updateLastMessage(crewId: String, chatroomId: String, message: Message) {
        val chatroomRef =
            database.getReference("crews").child(crewId).child("chatrooms").child(chatroomId)
        val updates = mapOf(
            "lastMessage" to message.text,
            "lastMessageTimestamp" to message.timestamp
        )
        chatroomRef.updateChildren(updates)
            .addOnSuccessListener {
                println("Last message updated successfully for chatroom $chatroomId")
            }
            .addOnFailureListener {
                println("Failed to update last message for chatroom $chatroomId: ${it.message}")
            }
    }

    /**
     * Fetch all chatrooms for a crew.
     */
    fun getChatrooms(crewId: String, onResult: (List<Chatroom>) -> Unit) {
        val crewsRef = database.getReference("crews").child(crewId).child("chatrooms")
        crewsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val chatrooms = snapshot.children.mapNotNull { chatroom ->
                    val id = chatroom.key
                    val name = chatroom.child("name").getValue(String::class.java)
                    val lastMessage = chatroom.child("lastMessage").getValue(String::class.java) ?: ""
                    val lastMessageTimestamp = chatroom.child("lastMessageTimestamp").getValue(Long::class.java) ?: 0L

                    if (id != null && name != null) {
                        Chatroom(
                            id = id,
                            name = name,
                            lastMessage = lastMessage,
                            lastMessageTimeStamp = lastMessageTimestamp
                        )
                    } else null
                }
                onResult(chatrooms)
            }

            override fun onCancelled(error: DatabaseError) {
                println("Failed to fetch chatrooms for crew $crewId: ${error.message}")
                onResult(emptyList())
            }
        })
    }

    /**
     * Create a new chatroom within a crew.
     */
    fun createChatroom(
        crewId: String,
        chatroom: Chatroom,
        context: Context,
        onComplete: (Boolean) -> Unit
    ) {
        val chatroomRef =
            database.getReference("crews").child(crewId).child("chatrooms").child(chatroom.id)
        val chatroomData = mapOf(
            "name" to chatroom.name,
            "lastMessage" to "",
            "lastMessageTimestamp" to 0L
        )
        chatroomRef.setValue(chatroomData)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        context,
                        "Chatroom '${chatroom.name}' created successfully.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        context,
                        "Failed to create chatroom '${chatroom.name}': ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                onComplete(task.isSuccessful)
            }
    }
}
