package com.adentweets.app.data.model

data class TrendingTopic(
    val id: String = "",
    val category: String = "",
    val title: String = "",
    val tweetCount: String = "",
    val description: String? = null
)

data class AdenList(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val createdBy: String = "",
    val memberIds: List<String> = emptyList(),
    val memberCount: Long = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val isPrivate: Boolean = false
)