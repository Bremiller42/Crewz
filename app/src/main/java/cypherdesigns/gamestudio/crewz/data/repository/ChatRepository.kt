// ChatRepository.kt
package cypherdesigns.gamestudio.crewz.data.repository

import android.content.Context
import android.widget.Toast
import com.google.firebase.database.*
import cypherdesigns.gamestudio.crewz.ui.chat.Chatroom
import cypherdesigns.gamestudio.crewz.ui.chat.Message

class ChatRepository {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    fun getMessages(crewId: String, chatroomId: String, onResult: (List<Message>) -> Unit) {
        val chatLogsRef = database.getReference("crews")
            .child(crewId).child("chatrooms")
            .child(chatroomId).child("messages")
        chatLogsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = snapshot.children.mapNotNull { it.getValue(Message::class.java) }
                onResult(messages)
            }
            override fun onCancelled(error: DatabaseError) {
                println("Error fetching messages: ${error.message}")
                onResult(emptyList())
            }
        })
    }

    fun sendMessage(
        crewId: String,
        chatroomId: String,
        message: Message,
        onComplete: (Boolean) -> Unit
    ) {
        val ref = database.getReference("crews")
            .child(crewId).child("chatrooms")
            .child(chatroomId).child("messages")

        ref.push().setValue(message)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    updateLastMessage(crewId, chatroomId, message)
                }
                onComplete(task.isSuccessful)
            }
            .addOnFailureListener {
                println("Failed to send message: ${it.message}")
                onComplete(false)
            }
    }

    private fun updateLastMessage(crewId: String, chatroomId: String, message: Message) {
        val chatroomRef = database.getReference("crews")
            .child(crewId).child("chatrooms")
            .child(chatroomId)

        val updates = mapOf(
            "lastMessage" to message.text,
            "lastMessageTimestamp" to message.timestamp
        )
        chatroomRef.updateChildren(updates)
            .addOnSuccessListener {
                println("Last message updated for chatroom $chatroomId")
            }
            .addOnFailureListener {
                println("Failed to update last message: ${it.message}")
            }
    }

    fun getChatrooms(crewId: String, onResult: (List<Chatroom>) -> Unit) {
        val crewsRef = database.getReference("crews")
            .child(crewId).child("chatrooms")

        crewsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val rooms = snapshot.children.mapNotNull { chatroom ->
                    val id = chatroom.key
                    val name = chatroom.child("name").getValue(String::class.java)
                    val lastMessage = chatroom.child("lastMessage").getValue(String::class.java) ?: ""
                    val lastTimestamp = chatroom.child("lastMessageTimestamp").getValue(Long::class.java) ?: 0L
                    if (id != null && name != null) {
                        Chatroom(id, name, lastMessage, lastTimestamp)
                    } else null
                }
                onResult(rooms)
            }
            override fun onCancelled(error: DatabaseError) {
                println("Failed to fetch chatrooms: ${error.message}")
                onResult(emptyList())
            }
        })
    }

    fun createChatroom(
        crewId: String,
        chatroom: Chatroom,
        context: Context,
        onComplete: (Boolean) -> Unit
    ) {
        val ref = database.getReference("crews")
            .child(crewId).child("chatrooms")
            .child(chatroom.id)
        val data = mapOf(
            "name" to chatroom.name,
            "lastMessage" to "",
            "lastMessageTimestamp" to 0L
        )
        ref.setValue(data)
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
                        "Failed to create chatroom: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                onComplete(task.isSuccessful)
            }
    }
}
