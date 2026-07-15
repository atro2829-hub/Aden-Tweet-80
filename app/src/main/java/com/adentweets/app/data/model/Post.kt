package com.adentweets.app.data.model

data class Post(
    val id: String = "",
    val authorId: String = "",
    val text: String = "",
    val mediaUrls: List<String> = emptyList(),
    val mediaType: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val likeCount: Long = 0,
    val replyCount: Long = 0,
    val repostCount: Long = 0,
    val viewCount: Long = 0,
    val replyToId: String? = null,
    val isRepost: Boolean = false,
    val repostOfId: String? = null,
    val repostAuthorName: String? = null,
    val repostAuthorUsername: String? = null
)

data class PostWithAuthor(
    val post: Post = Post(),
    val author: User = User(),
    val isLikedByCurrentUser: Boolean = false,
    val isRepostedByCurrentUser: Boolean = false,
    val isBookmarkedByCurrentUser: Boolean = false
)