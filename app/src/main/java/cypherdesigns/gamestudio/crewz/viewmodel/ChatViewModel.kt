package cypherdesigns.gamestudio.crewz.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import cypherdesigns.gamestudio.crewz.data.repository.ChatRepository
import cypherdesigns.gamestudio.crewz.ui.chat.Chatroom
import cypherdesigns.gamestudio.crewz.ui.chat.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChatViewModel : ViewModel() {

    private val repository = ChatRepository()

    private val _chatrooms = MutableStateFlow<List<Chatroom>>(emptyList())
    val chatrooms: StateFlow<List<Chatroom>> = _chatrooms.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    /**
     * Fetch all chatrooms for a crew.
     */
    fun fetchChatrooms(crewId: String) {
        repository.getChatrooms(crewId) { chatrooms ->
            _chatrooms.value = chatrooms
        }
    }

    /**
     * Fetch messages for a specific chatroom in a crew.
     */
    fun fetchMessages(crewId: String, chatroomId: String) {
        repository.getMessages(crewId, chatroomId) { messages ->
            _messages.value = messages
        }
    }

    /**
     * Send a message to a specific chatroom in a crew.
     */
    fun sendMessage(crewId: String, chatroomId: String, message: Message) {
        repository.sendMessage(crewId, chatroomId, message) { success ->
            if (!success) {
                println("Failed to send message to chatroom $chatroomId in crew $crewId")
            }
        }
    }

    /**
     * Create a new chatroom for a crew.
     */
    fun createChatroom(crewId: String, chatroomName: String, context: Context) {
        val chatroom = Chatroom(
            id = System.currentTimeMillis().toString(),
            name = chatroomName,
            lastMessage = "",
            lastMessageTimeStamp = 0L
        )
        repository.createChatroom(crewId, chatroom, context) { success ->
            if (success) {
                fetchChatrooms(crewId)
            } else {
                println("Failed to create chatroom $chatroomName for crew $crewId")
            }
        }
    }
}
