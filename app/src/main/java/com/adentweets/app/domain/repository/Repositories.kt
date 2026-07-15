package com.adentweets.app.domain.repository

import com.adentweets.app.data.model.*
import com.adentweets.app.util.Result
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val isLoggedIn: Boolean
    val currentUserId: String?
    suspend fun signIn(email: String, password: String): Result<Unit>
    suspend fun signUp(name: String, email: String, password: String, username: String): Result<Unit>
    suspend fun resetPassword(email: String): Result<Unit>
    fun signOut()
    suspend fun deleteAccount(): Result<Unit>
}

interface UserRepository {
    suspend fun getUser(userId: String): Result<User>
    fun observeUser(userId: String): Flow<User>
    suspend fun updateUserProfile(updates: Map<String, Any>): Result<Unit>
    suspend fun followUser(targetUid: String): Result<Unit>
    suspend fun unfollowUser(targetUid: String): Result<Unit>
    fun isFollowing(currentUid: String, targetUid: String): Flow<Boolean>
    fun observeFollowers(userId: String): Flow<List<User>>
    fun observeFollowing(userId: String): Flow<List<User>>
    suspend fun getUserPosts(userId: String): Flow<List<Post>>
}

interface PostRepository {
    suspend fun createPost(text: String, mediaUrls: List<String>, mediaType: String?, replyToId: String?): Result<Post>
    suspend fun deletePost(postId: String): Result<Unit>
    fun observeFeed(): Flow<List<Post>>
    fun observeUserPosts(userId: String): Flow<List<Post>>
    fun observeReplies(postId: String): Flow<List<Post>>
    suspend fun getPost(postId: String): Result<Post>
    suspend fun likePost(postId: String): Result<Unit>
    suspend fun unlikePost(postId: String): Result<Unit>
    fun observeIsLiked(postId: String, userId: String): Flow<Boolean>
    suspend fun repostPost(originalPostId: String, quoteText: String?): Result<Post>
    suspend fun bookmarkPost(postId: String): Result<Unit>
    suspend fun unbookmarkPost(postId: String): Result<Unit>
    fun observeBookmarks(userId: String): Flow<List<Post>>
}

interface MessageRepository {
    suspend fun getOrCreateConversation(otherUserId: String): Result<String>
    fun observeConversations(userId: String): Flow<List<ConversationWithParticipant>>
    suspend fun sendMessage(conversationId: String, text: String, mediaUrl: String?): Result<Message>
    fun observeMessages(conversationId: String): Flow<List<Message>>
    suspend fun getParticipant(conversationId: String, currentUserId: String): Result<User>
}

interface NotificationRepository {
    fun observeNotifications(userId: String): Flow<List<AppNotification>>
    suspend fun markAsRead(userId: String, notifId: String): Result<Unit>
}

interface SearchRepository {
    suspend fun searchUsers(query: String): Result<List<User>>
    suspend fun searchPosts(query: String): Result<List<Post>>
}

interface ListRepository {
    suspend fun createList(name: String, description: String, isPrivate: Boolean): Result<AdenList>
    fun observeUserLists(userId: String): Flow<List<AdenList>>
}