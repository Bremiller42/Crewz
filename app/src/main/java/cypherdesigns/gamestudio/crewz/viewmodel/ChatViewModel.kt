package cypherdesigns.gamestudio.crewz.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import cypherdesigns.gamestudio.crewz.data.repository.ChatRepository
import cypherdesigns.gamestudio.crewz.ui.chat.Chatroom
import cypherdesigns.gamestudio.crewz.ui.chat.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.google.firebase.firestore.FirebaseFirestore

class ChatViewModel : ViewModel() {

    private val repository = ChatRepository()

    private val _chatrooms = MutableStateFlow<List<Chatroom>>(emptyList())
    val chatrooms: StateFlow<List<Chatroom>> = _chatrooms.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()


    fun fetchChatrooms() {
        repository.getChatrooms { chatrooms ->
            _chatrooms.value = chatrooms
        }
    }

    fun fetchMessages(chatroomId: String) {
        repository.getMessages(chatroomId) { messages ->
            _messages.value = messages
        }
    }

    fun sendMessage(chatroomId: String, message: Message) {
        repository.sendMessage(chatroomId, message) { success ->
            // Handle success/failure if needed
        }
    }
    fun createChatroom(name: String, context: Context) {
        val chatroom = Chatroom(
            id = System.currentTimeMillis().toString(),
            name = name
        )
        repository.createChatroom(chatroom, context) { success ->
            if (success) {
                fetchChatrooms()
            } else {
                // Handle failure if needed
            }
        }
    }
}