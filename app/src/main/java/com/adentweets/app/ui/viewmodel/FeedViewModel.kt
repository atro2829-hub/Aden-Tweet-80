package com.adentweets.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adentweets.app.data.model.Post
import com.adentweets.app.data.model.PostWithAuthor
import com.adentweets.app.data.model.User
import com.adentweets.app.domain.repository.PostRepository
import com.adentweets.app.domain.repository.UserRepository
import com.adentweets.app.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FeedState(
    val posts: List<PostWithAuthor> = emptyList(),
    val isLoading: Boolean = true,
    val error: String = "",
    val selectedTab: Int = 0
)

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(FeedState())
    val state: StateFlow<FeedState> = _state

    private val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val likedPosts = mutableSetOf<String>()
    private val bookmarkedPosts = mutableSetOf<String>()

    init {
        observeFeed()
    }

    private fun observeFeed() {
        viewModelScope.launch {
            postRepository.observeFeed().collect { posts ->
                val postsWithAuthors = posts.filter { !it.isRepost || it.text.isNotBlank() }.mapNotNull { post ->
                    val authorResult = userRepository.getUser(post.authorId)
                    val author = if (authorResult is Result.Success) authorResult.data else User(id = post.authorId)
                    PostWithAuthor(
                        post = post,
                        author = author,
                        isLikedByCurrentUser = likedPosts.contains(post.id),
                        isRepostedByCurrentUser = false,
                        isBookmarkedByCurrentUser = bookmarkedPosts.contains(post.id)
                    )
                }
                _state.value = _state.value.copy(posts = postsWithAuthors, isLoading = false)
            }
        }
    }

    fun likePost(postId: String) {
        viewModelScope.launch {
            likedPosts.add(postId)
            updatePostState(postId) { it.copy(isLikedByCurrentUser = true, post = it.post.copy(likeCount = it.post.likeCount + 1)) }
            postRepository.likePost(postId)
        }
    }

    fun unlikePost(postId: String) {
        viewModelScope.launch {
            likedPosts.remove(postId)
            updatePostState(postId) { it.copy(isLikedByCurrentUser = false, post = it.post.copy(likeCount = it.post.likeCount - 1)) }
            postRepository.unlikePost(postId)
        }
    }

    fun toggleLike(postId: String) {
        val post = _state.value.posts.find { it.post.id == postId }
        if (post != null) {
            if (post.isLikedByCurrentUser) unlikePost(postId) else likePost(postId)
        }
    }

    fun repost(postId: String) {
        viewModelScope.launch {
            postRepository.repostPost(postId, null)
        }
    }

    fun bookmark(postId: String) {
        viewModelScope.launch {
            bookmarkedPosts.add(postId)
            updatePostState(postId) { it.copy(isBookmarkedByCurrentUser = true) }
            postRepository.bookmarkPost(postId)
        }
    }

    fun unbookmark(postId: String) {
        viewModelScope.launch {
            bookmarkedPosts.remove(postId)
            updatePostState(postId) { it.copy(isBookmarkedByCurrentUser = false) }
            postRepository.unbookmarkPost(postId)
        }
    }

    fun toggleBookmark(postId: String) {
        val post = _state.value.posts.find { it.post.id == postId }
        if (post != null) {
            if (post.isBookmarkedByCurrentUser) unbookmark(postId) else bookmark(postId)
        }
    }

    private fun updatePostState(postId: String, update: (PostWithAuthor) -> PostWithAuthor) {
        _state.value = _state.value.copy(
            posts = _state.value.posts.map { if (it.post.id == postId) update(it) else it }
        )
    }

    fun setTab(tab: Int) { _state.value = _state.value.copy(selectedTab = tab) }
}