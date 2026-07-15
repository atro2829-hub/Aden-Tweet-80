package com.adentweets.app.data.model

data class AppNotification(
    val id: String = "",
    val type: String = "",
    val actorId: String = "",
    val actorName: String = "",
    val actorUsername: String = "",
    val actorAvatar: String = "",
    val targetPostId: String? = null,
    val targetPostText: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)