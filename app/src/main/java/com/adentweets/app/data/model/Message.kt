package com.adentweets.app.data.model

data class Conversation(
    val id: String = "",
    val participant1Id: String = "",
    val participant2Id: String = "",
    val lastMessage: String = "",
    val lastMessageTimestamp: Long = 0L,
    val lastMessageSenderId: String = "",
    val unreadCount: Int = 0
)

data class Message(
    val id: String = "",
    val senderId: String = "",
    val text: String = "",
    val mediaUrl: String? = null,
    val mediaType: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

data class ConversationWithParticipant(
    val conversation: Conversation = Conversation(),
    val participant: User = User(),
    val lastMessageTime: Long = 0L
)