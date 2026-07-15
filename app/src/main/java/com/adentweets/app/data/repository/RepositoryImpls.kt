package com.adentweets.app.data.repository

import com.adentweets.app.data.model.*
import com.adentweets.app.data.remote.FirebaseService
import com.adentweets.app.domain.repository.*
import com.adentweets.app.util.Result
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseService: FirebaseService
) : AuthRepository {
    override val isLoggedIn: Boolean get() = firebaseService.isLoggedIn
    override val currentUserId: String? get() = firebaseService.currentUserId

    override suspend fun signIn(email: String, password: String): Result<Unit> {
        return when (val result = firebaseService.signIn(email, password)) {
            is Result.Success -> Result.Success(Unit)
            is Result.Error -> Result.Error(result.exception, result.message)
            is Result.Loading -> Result.Loading
        }
    }

    override suspend fun signUp(name: String, email: String, password: String, username: String): Result<Unit> {
        return when (val result = firebaseService.signUp(name, email, password, username)) {
            is Result.Success -> Result.Success(Unit)
            is Result.Error -> Result.Error(result.exception, result.message)
            is Result.Loading -> Result.Loading
        }
    }

    override suspend fun resetPassword(email: String): Result<Unit> {
        return when (val result = firebaseService.resetPassword(email)) {
            is Result.Success -> Result.Success(Unit)
            is Result.Error -> Result.Error(result.exception, result.message)
            is Result.Loading -> Result.Loading
        }
    }

    override fun signOut() = firebaseService.signOut()

    override suspend fun deleteAccount(): Result<Unit> {
        return when (val result = firebaseService.deleteAccount()) {
            is Result.Success -> Result.Success(Unit)
            is Result.Error -> Result.Error(result.exception, result.message)
            is Result.Loading -> Result.Loading
        }
    }
}

class PostRepositoryImpl @Inject constructor(
    private val firebaseService: FirebaseService
) : PostRepository {
    override suspend fun createPost(text: String, mediaUrls: List<String>, mediaType: String?, replyToId: String?): Result<Post> {
        val post = Post(text = text, mediaUrls = mediaUrls, mediaType = mediaType, replyToId = replyToId)
        return firebaseService.createPost(post)
    }

    override suspend fun deletePost(postId: String) = firebaseService.deletePost(postId)

    override fun observeFeed(): Flow<List<Post>> = firebaseService.observeFeed()

    override fun observeUserPosts(userId: String): Flow<List<Post>> = firebaseService.observeUserPosts(userId)

    override fun observeReplies(postId: String): Flow<List<Post>> = firebaseService.observeReplies(postId)

    override suspend fun getPost(postId: String) = firebaseService.getPost(postId)

    override suspend fun likePost(postId: String) = firebaseService.likePost(postId)

    override suspend fun unlikePost(postId: String) = firebaseService.unlikePost(postId)

    override fun observeIsLiked(postId: String, userId: String): Flow<Boolean> =
        firebaseService.observeIsLiked(postId, userId)

    override suspend fun repostPost(originalPostId: String, quoteText: String?): Result<Post> {
        return if (quoteText != null) {
            val post = Post(
                text = quoteText, repostOfId = originalPostId,
                isRepost = false, repostAuthorName = null, repostAuthorUsername = null
            )
            firebaseService.createPost(post)
        } else {
            firebaseService.repostPost(originalPostId)
        }
    }

    override suspend fun bookmarkPost(postId: String) = firebaseService.bookmarkPost(postId)

    override suspend fun unbookmarkPost(postId: String) = firebaseService.unbookmarkPost(postId)

    override fun observeBookmarks(userId: String): Flow<List<Post>> = firebaseService.observeBookmarks(userId)
}

class UserRepositoryImpl @Inject constructor(
    private val firebaseService: FirebaseService
) : UserRepository {
    override suspend fun getUser(userId: String) = firebaseService.getUser(userId)

    override fun observeUser(userId: String): Flow<User> = firebaseService.observeUser(userId)

    override suspend fun updateUserProfile(updates: Map<String, Any>) = firebaseService.updateUserProfile(updates)

    override suspend fun followUser(targetUid: String) = firebaseService.followUser(targetUid)

    override suspend fun unfollowUser(targetUid: String) = firebaseService.unfollowUser(targetUid)

    override fun isFollowing(currentUid: String, targetUid: String): Flow<Boolean> =
        firebaseService.isFollowing(currentUid, targetUid)

    override fun observeFollowers(userId: String): Flow<List<User>> = firebaseService.observeFollowers(userId)

    override fun observeFollowing(userId: String): Flow<List<User>> = firebaseService.observeFollowing(userId)

    override suspend fun getUserPosts(userId: String): Flow<List<Post>> = firebaseService.observeUserPosts(userId)
}

class MessageRepositoryImpl @Inject constructor(
    private val firebaseService: FirebaseService
) : MessageRepository {
    override suspend fun getOrCreateConversation(otherUserId: String) =
        firebaseService.getOrCreateConversation(otherUserId)

    override fun observeConversations(userId: String): Flow<List<ConversationWithParticipant>> =
        firebaseService.observeConversations(userId)

    override suspend fun sendMessage(conversationId: String, text: String, mediaUrl: String?): Result<Message> =
        firebaseService.sendMessage(conversationId, text, mediaUrl)

    override fun observeMessages(conversationId: String): Flow<List<Message>> =
        firebaseService.observeMessages(conversationId)

    override suspend fun getParticipant(conversationId: String, currentUserId: String): Result<User> {
        val otherId = if (conversationId.startsWith(currentUserId)) {
            conversationId.substring(currentUserId.length + 1)
        } else {
            conversationId.substring(0, conversationId.indexOf("_" + currentUserId))
        }
        return firebaseService.getUser(otherId)
    }
}

class NotificationRepositoryImpl @Inject constructor(
    private val firebaseService: FirebaseService
) : NotificationRepository {
    override fun observeNotifications(userId: String): Flow<List<AppNotification>> =
        firebaseService.observeNotifications(userId)

    override suspend fun markAsRead(userId: String, notifId: String) =
        firebaseService.markNotificationRead(userId, notifId)
}

class SearchRepositoryImpl @Inject constructor(
    private val firebaseService: FirebaseService
) : SearchRepository {
    override suspend fun searchUsers(query: String) = firebaseService.searchUsers(query)
    override suspend fun searchPosts(query: String) = firebaseService.searchPosts(query)
}

class ListRepositoryImpl @Inject constructor(
    private val firebaseService: FirebaseService
) : ListRepository {
    override suspend fun createList(name: String, description: String, isPrivate: Boolean): Result<AdenList> {
        val list = AdenList(name = name, description = description, isPrivate = isPrivate)
        return firebaseService.createList(list)
    }

    override fun observeUserLists(userId: String): Flow<List<AdenList>> =
        firebaseService.observeUserLists(userId)
}