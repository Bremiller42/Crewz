package cypherdesigns.gamestudio.crewz.ui.chat

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Chatroom(
    val id: String = "",
    val name: String = "",
    var lastMessage: String = "",
    var lastMessageTimeStamp: Long = 0L
)