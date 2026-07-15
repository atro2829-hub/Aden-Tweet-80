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

data class ProfileState(
    val user: User = User(),
    val posts: List<PostWithAuthor> = emptyList(),
    val followers: List<User> = emptyList(),
    val following: List<User> = emptyList(),
    val isFollowing: Boolean = false,
    val isLoading: Boolean = true,
    val error: String = "",
    val selectedTab: Int = 0
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val postRepository: PostRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state

    private val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""

    fun loadProfile(userId: String) {
        viewModelScope.launch {
            userRepository.observeUser(userId).collect { user ->
                _state.value = _state.value.copy(user = user, isLoading = false)
                loadUserPosts(userId, user)
            }
        }
        loadFollowStatus(userId)
    }

    private fun loadUserPosts(userId: String, user: User) {
        viewModelScope.launch {
            postRepository.observeUserPosts(userId).collect { posts ->
                val postsWithAuthors = posts.map { post ->
                    PostWithAuthor(post = post, author = user)
                }
                _state.value = _state.value.copy(posts = postsWithAuthors)
            }
        }
    }

    private fun loadFollowStatus(userId: String) {
        if (userId == currentUserId) return
        viewModelScope.launch {
            userRepository.isFollowing(currentUserId, userId).collect { following ->
                _state.value = _state.value.copy(isFollowing = following)
            }
        }
    }

    fun toggleFollow(targetUid: String) {
        val isCurrentlyFollowing = _state.value.isFollowing
        viewModelScope.launch {
            if (isCurrentlyFollowing) {
                _state.value = _state.value.copy(isFollowing = false, user = _state.value.user.copy(followersCount = (_state.value.user.followersCount - 1).coerceAtLeast(0)))
                userRepository.unfollowUser(targetUid)
            } else {
                _state.value = _state.value.copy(isFollowing = true, user = _state.value.user.copy(followersCount = _state.value.user.followersCount + 1))
                userRepository.followUser(targetUid)
            }
        }
    }

    fun loadFollowers(userId: String) {
        viewModelScope.launch {
            userRepository.observeFollowers(userId).collect { followers ->
                _state.value = _state.value.copy(followers = followers)
            }
        }
    }

    fun loadFollowing(userId: String) {
        viewModelScope.launch {
            userRepository.observeFollowing(userId).collect { following ->
                _state.value = _state.value.copy(following = following)
            }
        }
    }

    fun setTab(tab: Int) { _state.value = _state.value.copy(selectedTab = tab) }
}