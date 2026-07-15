package com.adentweets.app.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import com.adentweets.app.data.model.*
import com.adentweets.app.util.Constants
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseService @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase
) {
    private val dbRef get() = database.reference

    // ─── Auth ────────────────────────────────────────────────────────────
    val currentUser: FirebaseUser? get() = auth.currentUser
    val currentUserId: String? get() = auth.currentUser?.uid
    val isLoggedIn: Boolean get() = auth.currentUser != null

    suspend fun signIn(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.Success(result.user!!)
        } catch (e: Exception) {
            Result.Error(e, e.localizedMessage ?: "Sign in failed")
        }
    }

    suspend fun signUp(name: String, email: String, password: String, username: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user!!.uid
            val user = User(
                id = uid, name = name, username = username,
                email = email, createdAt = System.currentTimeMillis()
            )
            dbRef.child(Constants.DB_USERS).child(uid).setValue(user).await()
            result.user!!.updateProfile(userProfileChangeRequest { displayName = name }).await()
            Result.Success(result.user!!)
        } catch (e: Exception) {
            Result.Error(e, e.localizedMessage ?: "Sign up failed")
        }
    }

    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, e.localizedMessage ?: "Password reset failed")
        }
    }

    fun signOut() {
        auth.signOut()
    }

    suspend fun deleteAccount(): Result<Unit> {
        return try {
            val uid = currentUserId ?: return Result.Error(message = "Not logged in")
            dbRef.child(Constants.DB_USERS).child(uid).removeValue().await()
            auth.currentUser?.delete()?.await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, e.localizedMessage ?: "Account deletion failed")
        }
    }

    // ─── User Operations ────────────────────────────────────────────────
    suspend fun getUser(userId: String): Result<User> {
        return try {
            val snapshot = dbRef.child(Constants.DB_USERS).child(userId).get().await()
            val user = snapshot.getValue(User::class.java) ?: User(id = userId)
            Result.Success(user)
        } catch (e: Exception) {
            Result.Error(e, "Failed to fetch user")
        }
    }

    fun observeUser(userId: String): Flow<User> = callbackFlow {
        val ref = dbRef.child(Constants.DB_USERS).child(userId)
        val listener = ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java) ?: return
                trySend(user)
            }
            override fun onCancelled(error: DatabaseError) = Unit
        })
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun updateUserProfile(updates: Map<String, Any>): Result<Unit> {
        return try {
            val uid = currentUserId ?: return Result.Error(message = "Not logged in")
            dbRef.child(Constants.DB_USERS).child(uid).updateChildren(updates).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Failed to update profile")
        }
    }

    suspend fun searchUsers(query: String): Result<List<User>> {
        return try {
            val snapshot = dbRef.child(Constants.DB_USERS)
                .orderByChild("username")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .limitToFirst(Constants.USERS_PAGE_SIZE)
                .get().await()
            val users = snapshot.children.mapNotNull { it.getValue(User::class.java) }
            Result.Success(users)
        } catch (e: Exception) {
            Result.Error(e, "Search failed")
        }
    }

    // ─── Follow System ───────────────────────────────────────────────────
    suspend fun followUser(targetUid: String): Result<Unit> {
        return try {
            val uid = currentUserId ?: return Result.Error(message = "Not logged in")
            val updates = mapOf(
                "/${Constants.DB_USERS}/$uid/following/$targetUid" to true,
                "/${Constants.DB_USERS}/$targetUid/followers/$uid" to true
            )
            dbRef.updateChildren(updates).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Follow failed")
        }
    }

    suspend fun unfollowUser(targetUid: String): Result<Unit> {
        return try {
            val uid = currentUserId ?: return Result.Error(message = "Not logged in")
            val updates = mapOf(
                "/${Constants.DB_USERS}/$uid/following/$targetUid" to null,
                "/${Constants.DB_USERS}/$targetUid/followers/$uid" to null
            )
            dbRef.updateChildren(updates).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Unfollow failed")
        }
    }

    fun isFollowing(currentUid: String, targetUid: String): Flow<Boolean> = callbackFlow {
        val ref = dbRef.child(Constants.DB_USERS).child(currentUid).child("following").child(targetUid)
        val listener = ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.exists())
            }
            override fun onCancelled(error: DatabaseError) = Unit
        })
        awaitClose { ref.removeEventListener(listener) }
    }

    fun observeFollowers(userId: String, limit: Int = 50): Flow<List<User>> = callbackFlow {
        val ref = dbRef.child(Constants.DB_USERS).child(userId).child("followers")
            .limitToFirst(limit)
        val listener = ref.addChildEventListener(object : ChildEventListener {
            private val uids = mutableListOf<String>()
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val uid = snapshot.key ?: return
                uids.add(uid)
                fetchUsers(uids)
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {
                val uid = snapshot.key ?: return
                uids.remove(uid)
                fetchUsers(uids)
            }
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
            private suspend fun fetchUsers(ids: List<String>) {
                val users = ids.mapNotNull { uid ->
                    dbRef.child(Constants.DB_USERS).child(uid).get().await()
                        .getValue(User::class.java)
                }
                trySend(users)
            }
        })
        awaitClose { ref.removeEventListener(listener) }
    }

    fun observeFollowing(userId: String, limit: Int = 50): Flow<List<User>> = callbackFlow {
        val ref = dbRef.child(Constants.DB_USERS).child(userId).child("following")
            .limitToFirst(limit)
        val listener = ref.addChildEventListener(object : ChildEventListener {
            private val uids = mutableListOf<String>()
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val uid = snapshot.key ?: return
                uids.add(uid)
                fetchUsers(uids)
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {
                val uid = snapshot.key ?: return
                uids.remove(uid)
                fetchUsers(uids)
            }
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
            private suspend fun fetchUsers(ids: List<String>) {
                val users = ids.mapNotNull { uid ->
                    dbRef.child(Constants.DB_USERS).child(uid).get().await()
                        .getValue(User::class.java)
                }
                trySend(users)
            }
        })
        awaitClose { ref.removeEventListener(listener) }
    }

    // ─── Post Operations ────────────────────────────────────────────────
    suspend fun createPost(post: Post): Result<Post> {
        return try {
            val uid = currentUserId ?: return Result.Error(message = "Not logged in")
            val postId = dbRef.child(Constants.DB_POSTS).push().key ?: return Result.Error(message = "Failed to generate ID")
            val newPost = post.copy(id = postId, authorId = uid, createdAt = System.currentTimeMillis())
            val updates = mutableMapOf<String, Any>(
                "/${Constants.DB_POSTS}/$postId" to newPost,
                "/${Constants.DB_USERS}/$uid/posts/$postId" to true
            )
            if (post.replyToId != null) {
                updates["/${Constants.DB_POSTS}/${post.replyToId}/replies/$postId"] = true
                dbRef.child(Constants.DB_POSTS).child(post.replyToId).child("replyCount")
                    .runTransaction(object : Transaction.Handler {
                        override fun doTransaction(currentData: MutableData): Transaction.Result {
                            val count = currentData.getValue(Long::class.java) ?: 0L
                            currentData.value = count + 1
                            return Transaction.success(currentData)
                        }
                        override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot) {}
                    }).await()
            }
            dbRef.updateChildren(updates).await()
            dbRef.child(Constants.DB_USERS).child(uid).child("postsCount")
                .runTransaction(object : Transaction.Handler {
                    override fun doTransaction(currentData: MutableData): Transaction.Result {
                        val count = currentData.getValue(Long::class.java) ?: 0L
                        currentData.value = count + 1
                        return Transaction.success(currentData)
                    }
                    override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot) {}
                }).await()
            Result.Success(newPost)
        } catch (e: Exception) {
            Result.Error(e, "Failed to create post")
        }
    }

    suspend fun deletePost(postId: String): Result<Unit> {
        return try {
            val uid = currentUserId ?: return Result.Error(message = "Not logged in")
            val postRef = dbRef.child(Constants.DB_POSTS).child(postId)
            val snapshot = postRef.get().await()
            val post = snapshot.getValue(Post::class.java) ?: return Result.Error(message = "Post not found")
            val updates = mutableMapOf<String, Any?>(
                "/${Constants.DB_POSTS}/$postId" to null,
                "/${Constants.DB_USERS}/$uid/posts/$postId" to null
            )
            if (post.replyToId != null) {
                updates["/${Constants.DB_POSTS}/${post.replyToId}/replies/$postId"] = null
            }
            dbRef.updateChildren(updates).await()
            dbRef.child(Constants.DB_USERS).child(uid).child("postsCount")
                .runTransaction(object : Transaction.Handler {
                    override fun doTransaction(currentData: MutableData): Transaction.Result {
                        val count = currentData.getValue(Long::class.java) ?: 0L
                        currentData.value = if (count > 0) count - 1 else 0
                        return Transaction.success(currentData)
                    }
                    override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot) {}
                }).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Failed to delete post")
        }
    }

    fun observeFeed(orderBy: String = "createdAt", limit: Int = Constants.POSTS_PAGE_SIZE): Flow<List<Post>> = callbackFlow {
        val ref = dbRef.child(Constants.DB_POSTS)
            .orderByChild(orderBy)
            .limitToLast(limit)
        val listener = ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val posts = snapshot.children.reversed()
                    .mapNotNull { it.getValue(Post::class.java) }
                trySend(posts)
            }
            override fun onCancelled(error: DatabaseError) = Unit
        })
        awaitClose { ref.removeEventListener(listener) }
    }

    fun observeUserPosts(userId: String, limit: Int = Constants.POSTS_PAGE_SIZE): Flow<List<Post>> = callbackFlow {
        val ref = dbRef.child(Constants.DB_POSTS)
            .orderByChild("authorId")
            .equalTo(userId)
            .limitToLast(limit)
        val listener = ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val posts = snapshot.children.reversed()
                    .mapNotNull { it.getValue(Post::class.java) }
                trySend(posts)
            }
            override fun onCancelled(error: DatabaseError) = Unit
        })
        awaitClose { ref.removeEventListener(listener) }
    }

    fun observeReplies(postId: String): Flow<List<Post>> = callbackFlow {
        val ref = dbRef.child(Constants.DB_POSTS).child(postId).child("replies")
            .limitToLast(Constants.POSTS_PAGE_SIZE)
        val listener = ref.addChildEventListener(object : ChildEventListener {
            private val replyIds = mutableListOf<String>()
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val id = snapshot.key ?: return
                replyIds.add(id)
                fetchReplies(replyIds)
            }
            override fun onChildChanged(s: DataSnapshot, p: String?) {}
            override fun onChildRemoved(s: DataSnapshot) {}
            override fun onChildMoved(s: DataSnapshot, p: String?) {}
            override fun onCancelled(error: DatabaseError) {}
            private suspend fun fetchReplies(ids: List<String>) {
                val replies = ids.mapNotNull { rid ->
                    dbRef.child(Constants.DB_POSTS).child(rid).get().await()
                        .getValue(Post::class.java)
                }.sortedByDescending { it.createdAt }
                trySend(replies)
            }
        })
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun getPost(postId: String): Result<Post> {
        return try {
            val snapshot = dbRef.child(Constants.DB_POSTS).child(postId).get().await()
            val post = snapshot.getValue(Post::class.java) ?: return Result.Error(message = "Post not found")
            Result.Success(post)
        } catch (e: Exception) {
            Result.Error(e, "Failed to fetch post")
        }
    }

    // ─── Like System ────────────────────────────────────────────────────
    suspend fun likePost(postId: String): Result<Unit> {
        return try {
            val uid = currentUserId ?: return Result.Error(message = "Not logged in")
            val updates = mapOf(
                "/${Constants.DB_POSTS}/$postId/likes/$uid" to true,
                "/${Constants.DB_USERS}/$uid/likedPosts/$postId" to true
            )
            dbRef.updateChildren(updates).await()
            dbRef.child(Constants.DB_POSTS).child(postId).child("likeCount")
                .runTransaction(object : Transaction.Handler {
                    override fun doTransaction(currentData: MutableData): Transaction.Result {
                        val count = currentData.getValue(Long::class.java) ?: 0L
                        currentData.value = count + 1
                        return Transaction.success(currentData)
                    }
                    override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot) {}
                }).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Like failed")
        }
    }

    suspend fun unlikePost(postId: String): Result<Unit> {
        return try {
            val uid = currentUserId ?: return Result.Error(message = "Not logged in")
            val updates = mapOf(
                "/${Constants.DB_POSTS}/$postId/likes/$uid" to null,
                "/${Constants.DB_USERS}/$uid/likedPosts/$postId" to null
            )
            dbRef.updateChildren(updates).await()
            dbRef.child(Constants.DB_POSTS).child(postId).child("likeCount")
                .runTransaction(object : Transaction.Handler {
                    override fun doTransaction(currentData: MutableData): Transaction.Result {
                        val count = currentData.getValue(Long::class.java) ?: 0L
                        currentData.value = if (count > 0) count - 1 else 0
                        return Transaction.success(currentData)
                    }
                    override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot) {}
                }).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Unlike failed")
        }
    }

    fun observeIsLiked(postId: String, userId: String): Flow<Boolean> = callbackFlow {
        val ref = dbRef.child(Constants.DB_POSTS).child(postId).child("likes").child(userId)
        val listener = ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) { trySend(snapshot.exists()) }
            override fun onCancelled(error: DatabaseError) = Unit
        })
        awaitClose { ref.removeEventListener(listener) }
    }

    // ─── Repost System ──────────────────────────────────────────────────
    suspend fun repostPost(originalPostId: String): Result<Post> {
        return try {
            val uid = currentUserId ?: return Result.Error(message = "Not logged in")
            val original = dbRef.child(Constants.DB_POSTS).child(originalPostId).get().await()
                .getValue(Post::class.java) ?: return Result.Error(message = "Original post not found")
            val originalAuthor = dbRef.child(Constants.DB_USERS).child(original.authorId).get().await()
                .getValue(User::class.java)
            val repost = Post(
                isRepost = true,
                repostOfId = originalPostId,
                repostAuthorName = originalAuthor?.name ?: "",
                repostAuthorUsername = originalAuthor?.username ?: "",
                authorId = uid
            )
            val result = createPost(repost)
            if (result is Result.Success) {
                dbRef.child(Constants.DB_POSTS).child(originalPostId).child("repostCount")
                    .runTransaction(object : Transaction.Handler {
                        override fun doTransaction(currentData: MutableData): Transaction.Result {
                            val count = currentData.getValue(Long::class.java) ?: 0L
                            currentData.value = count + 1
                            return Transaction.success(currentData)
                        }
                        override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot) {}
                    }).await()
            }
            result
        } catch (e: Exception) {
            Result.Error(e, "Repost failed")
        }
    }

    // ─── Bookmark System ────────────────────────────────────────────────
    suspend fun bookmarkPost(postId: String): Result<Unit> {
        return try {
            val uid = currentUserId ?: return Result.Error(message = "Not logged in")
            dbRef.child(Constants.DB_USERS).child(uid).child("bookmarks").child(postId)
                .setValue(true).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Bookmark failed")
        }
    }

    suspend fun unbookmarkPost(postId: String): Result<Unit> {
        return try {
            val uid = currentUserId ?: return Result.Error(message = "Not logged in")
            dbRef.child(Constants.DB_USERS).child(uid).child("bookmarks").child(postId)
                .removeValue().await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Unbookmark failed")
        }
    }

    fun observeBookmarks(userId: String): Flow<List<Post>> = callbackFlow {
        val ref = dbRef.child(Constants.DB_USERS).child(userId).child("bookmarks")
            .limitToLast(Constants.POSTS_PAGE_SIZE)
        val listener = ref.addChildEventListener(object : ChildEventListener {
            private val ids = mutableListOf<String>()
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val id = snapshot.key ?: return
                ids.add(id)
                fetchPosts(ids)
            }
            override fun onChildChanged(s: DataSnapshot, p: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {
                val id = snapshot.key ?: return
                ids.remove(id)
                fetchPosts(ids)
            }
            override fun onChildMoved(s: DataSnapshot, p: String?) {}
            override fun onCancelled(error: DatabaseError) {}
            private suspend fun fetchPosts(postIds: List<String>) {
                val posts = postIds.mapNotNull { pid ->
                    dbRef.child(Constants.DB_POSTS).child(pid).get().await()
                        .getValue(Post::class.java)
                }.sortedByDescending { it.createdAt }
                trySend(posts)
            }
        })
        awaitClose { ref.removeEventListener(listener) }
    }

    // ─── Notification System ────────────────────────────────────────────
    fun observeNotifications(userId: String): Flow<List<AppNotification>> = callbackFlow {
        val ref = dbRef.child(Constants.DB_USERS).child(userId).child(Constants.DB_NOTIFICATIONS)
            .orderByChild("createdAt").limitToLast(50)
        val listener = ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val notifs = snapshot.children.reversed()
                    .mapNotNull { it.getValue(AppNotification::class.java) }
                trySend(notifs)
            }
            override fun onCancelled(error: DatabaseError) = Unit
        })
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun markNotificationRead(userId: String, notifId: String): Result<Unit> {
        return try {
            dbRef.child(Constants.DB_USERS).child(userId).child(Constants.DB_NOTIFICATIONS)
                .child(notifId).child("isRead").setValue(true).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Failed to mark read")
        }
    }

    // ─── Message System ─────────────────────────────────────────────────
    suspend fun getOrCreateConversation(otherUserId: String): Result<String> {
        return try {
            val uid = currentUserId ?: return Result.Error(message = "Not logged in")
            val convId = if (uid < otherUserId) "${uid}_$otherUserId" else "${otherUserId}_$uid"
            val snapshot = dbRef.child(Constants.DB_CONVERSATIONS).child(convId).get().await()
            if (!snapshot.exists()) {
                val conv = Conversation(
                    id = convId, participant1Id = uid, participant2Id = otherUserId,
                    lastMessage = "", lastMessageTimestamp = System.currentTimeMillis(),
                    lastMessageSenderId = uid
                )
                dbRef.child(Constants.DB_CONVERSATIONS).child(convId).setValue(conv).await()
            }
            Result.Success(convId)
        } catch (e: Exception) {
            Result.Error(e, "Failed to create conversation")
        }
    }

    fun observeConversations(userId: String): Flow<List<ConversationWithParticipant>> = callbackFlow {
        val ref = dbRef.child(Constants.DB_CONVERSATIONS)
            .orderByChild("lastMessageTimestamp")
        val listener = ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val convs = mutableListOf<ConversationWithParticipant>()
                for (child in snapshot.children.reversed()) {
                    val conv = child.getValue(Conversation::class.java) ?: continue
                    if (conv.participant1Id == userId || conv.participant2Id == userId) {
                        val otherId = if (conv.participant1Id == userId) conv.participant2Id else conv.participant1Id
                        convs.add(ConversationWithParticipant(conversation = conv, lastMessageTime = conv.lastMessageTimestamp))
                    }
                }
                trySend(convs)
            }
            override fun onCancelled(error: DatabaseError) = Unit
        })
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun sendMessage(conversationId: String, text: String, mediaUrl: String? = null): Result<Message> {
        return try {
            val uid = currentUserId ?: return Result.Error(message = "Not logged in")
            val msgId = dbRef.child(Constants.DB_CONVERSATIONS).child(conversationId).child("messages")
                .push().key ?: return Result.Error(message = "Failed to generate ID")
            val message = Message(
                id = msgId, senderId = uid, text = text,
                mediaUrl = mediaUrl, createdAt = System.currentTimeMillis()
            )
            val updates = mapOf(
                "/${Constants.DB_CONVERSATIONS}/$conversationId/messages/$msgId" to message,
                "/${Constants.DB_CONVERSATIONS}/$conversationId/lastMessage" to text,
                "/${Constants.DB_CONVERSATIONS}/$conversationId/lastMessageTimestamp" to ServerTimestamp.TIMESTAMP,
                "/${Constants.DB_CONVERSATIONS}/$conversationId/lastMessageSenderId" to uid
            )
            dbRef.updateChildren(updates).await()
            Result.Success(message)
        } catch (e: Exception) {
            Result.Error(e, "Failed to send message")
        }
    }

    fun observeMessages(conversationId: String): Flow<List<Message>> = callbackFlow {
        val ref = dbRef.child(Constants.DB_CONVERSATIONS).child(conversationId).child("messages")
            .orderByChild("createdAt").limitToLast(Constants.MESSAGES_PAGE_SIZE)
        val listener = ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = snapshot.children.mapNotNull { it.getValue(Message::class.java) }
                trySend(messages)
            }
            override fun onCancelled(error: DatabaseError) = Unit
        })
        awaitClose { ref.removeEventListener(listener) }
    }

    // ─── Search ─────────────────────────────────────────────────────────
    suspend fun searchPosts(query: String): Result<List<Post>> {
        return try {
            val snapshot = dbRef.child(Constants.DB_POSTS)
                .orderByChild("text")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .limitToFirst(Constants.POSTS_PAGE_SIZE)
                .get().await()
            val posts = snapshot.children.mapNotNull { it.getValue(Post::class.java) }
            Result.Success(posts)
        } catch (e: Exception) {
            Result.Error(e, "Search failed")
        }
    }

    // ─── Lists ──────────────────────────────────────────────────────────
    suspend fun createList(list: AdenList): Result<AdenList> {
        return try {
            val uid = currentUserId ?: return Result.Error(message = "Not logged in")
            val listId = dbRef.child("lists").push().key ?: return Result.Error()
            val newList = list.copy(id = listId, createdBy = uid, createdAt = System.currentTimeMillis())
            dbRef.child("lists").child(listId).setValue(newList).await()
            dbRef.child(Constants.DB_USERS).child(uid).child("lists").child(listId).setValue(true).await()
            Result.Success(newList)
        } catch (e: Exception) {
            Result.Error(e, "Failed to create list")
        }
    }

    fun observeUserLists(userId: String): Flow<List<AdenList>> = callbackFlow {
        val ref = dbRef.child(Constants.DB_USERS).child(userId).child("lists")
        val listener = ref.addChildEventListener(object : ChildEventListener {
            private val listIds = mutableListOf<String>()
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val id = snapshot.key ?: return
                listIds.add(id)
                fetchLists(listIds)
            }
            override fun onChildChanged(s: DataSnapshot, p: String?) {}
            override fun onChildRemoved(s: DataSnapshot) {}
            override fun onChildMoved(s: DataSnapshot, p: String?) {}
            override fun onCancelled(error: DatabaseError) {}
            private suspend fun fetchLists(ids: List<String>) {
                val lists = ids.mapNotNull { lid ->
                    dbRef.child("lists").child(lid).get().await().getValue(AdenList::class.java)
                }
                trySend(lists)
            }
        })
        awaitClose { ref.removeEventListener(listener) }
    }

    // ─── Settings ───────────────────────────────────────────────────────
    suspend fun updateUserSettings(settings: Map<String, Any>): Result<Unit> {
        return try {
            val uid = currentUserId ?: return Result.Error(message = "Not logged in")
            dbRef.child(Constants.DB_USERS).child(uid).child("settings").updateChildren(settings).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Failed to update settings")
        }
    }
}