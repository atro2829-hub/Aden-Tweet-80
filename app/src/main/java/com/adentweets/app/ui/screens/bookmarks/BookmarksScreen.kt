package com.adentweets.app.ui.screens.bookmarks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.adentweets.app.data.model.Post
import com.adentweets.app.data.model.PostWithAuthor
import com.adentweets.app.data.model.User
import com.adentweets.app.data.remote.FirebaseService
import com.adentweets.app.ui.components.*
import com.adentweets.app.util.Constants
import com.adentweets.app.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    private val firebaseService: FirebaseService
) : ViewModel() {
    private val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private val _bookmarks = MutableStateFlow<List<PostWithAuthor>>(emptyList())
    val bookmarks: StateFlow<List<PostWithAuthor>> = _bookmarks
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        viewModelScope.launch {
            firebaseService.observeBookmarks(currentUserId).collect { posts ->
                val withAuthors = posts.mapNotNull { post ->
                    val result = firebaseService.getUser(post.authorId)
                    val author = if (result is Result.Success) result.data else User(id = post.authorId)
                    PostWithAuthor(post = post, author = author, isBookmarkedByCurrentUser = true)
                }
                _bookmarks.value = withAuthors
                _isLoading.value = false
            }
        }
    }
}

@Composable
fun BookmarksScreen(navController: NavController) {
    val viewModel: BookmarksViewModel = hiltViewModel()
    val bookmarks by viewModel.bookmarks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        XTopBar(title = "Bookmarks", showBack = true, onBack = { navController.popBackStack() })
        if (isLoading) {
            LoadingIndicator()
        } else if (bookmarks.isEmpty()) {
            EmptyState(icon = Icons.Default.BookmarkBorder, title = "No bookmarks yet")
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(bookmarks, key = { it.post.id }) { pwa ->
                    TweetCard(
                        postWithAuthor = pwa,
                        onLike = {}, onRepost = {}, onReply = {}, onBookmark = {},
                        onProfileClick = { navController.navigate(Constants.Routes.userProfile(it)) },
                        onTweetClick = { navController.navigate(Constants.Routes.tweetDetail(it)) }
                    )
                }
            }
        }
    }
}