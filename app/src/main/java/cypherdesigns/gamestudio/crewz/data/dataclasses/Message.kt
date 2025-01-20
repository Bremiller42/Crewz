package cypherdesigns.gamestudio.crewz.ui.chat

data class Message(
    val senderUserName: String = "",
    val senderId: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis(),
)
