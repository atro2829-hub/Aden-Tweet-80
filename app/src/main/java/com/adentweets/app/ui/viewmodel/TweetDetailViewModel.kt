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

data class TweetDetailState(
    val post: PostWithAuthor? = null,
    val replies: List<PostWithAuthor> = emptyList(),
    val isLoading: Boolean = true,
    val error: String = ""
)

@HiltViewModel
class TweetDetailViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TweetDetailState())
    val state: StateFlow<TweetDetailState> = _state

    private val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private var isLiked = false
    private var isBookmarked = false

    fun loadTweet(postId: String) {
        viewModelScope.launch {
            when (val result = postRepository.getPost(postId)) {
                is Result.Success -> {
                    val authorResult = userRepository.getUser(result.data.authorId)
                    val author = if (authorResult is Result.Success) authorResult.data else User(id = result.data.authorId)
                    _state.value = _state.value.copy(
                        post = PostWithAuthor(post = result.data, author = author),
                        isLoading = false
                    )
                    loadReplies(postId)
                }
                is Result.Error -> {
                    _state.value = _state.value.copy(isLoading = false, error = result.message)
                }
                is Result.Loading -> Unit
            }
        }
    }

    private fun loadReplies(postId: String) {
        viewModelScope.launch {
            postRepository.observeReplies(postId).collect { replies ->
                val repliesWithAuthors = replies.mapNotNull { reply ->
                    val authorResult = userRepository.getUser(reply.authorId)
                    val author = if (authorResult is Result.Success) authorResult.data else null
                    if (author != null) PostWithAuthor(post = reply, author = author) else null
                }
                _state.value = _state.value.copy(replies = repliesWithAuthors)
            }
        }
    }

    fun toggleLike() {
        val post = _state.value.post ?: return
        viewModelScope.launch {
            if (isLiked) {
                isLiked = false
                _state.value = _state.value.copy(
                    post = post.copy(
                        isLikedByCurrentUser = false,
                        post = post.post.copy(likeCount = (post.post.likeCount - 1).coerceAtLeast(0))
                    )
                )
                postRepository.unlikePost(post.post.id)
            } else {
                isLiked = true
                _state.value = _state.value.copy(
                    post = post.copy(
                        isLikedByCurrentUser = true,
                        post = post.post.copy(likeCount = post.post.likeCount + 1)
                    )
                )
                postRepository.likePost(post.post.id)
            }
        }
    }

    fun toggleBookmark() {
        val post = _state.value.post ?: return
        viewModelScope.launch {
            if (isBookmarked) {
                isBookmarked = false
                _state.value = _state.value.copy(post = post.copy(isBookmarkedByCurrentUser = false))
                postRepository.unbookmarkPost(post.post.id)
            } else {
                isBookmarked = true
                _state.value = _state.value.copy(post = post.copy(isBookmarkedByCurrentUser = true))
                postRepository.bookmarkPost(post.post.id)
            }
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch { postRepository.deletePost(postId) }
    }

    fun repost(postId: String) {
        viewModelScope.launch { postRepository.repostPost(postId, null) }
    }
}