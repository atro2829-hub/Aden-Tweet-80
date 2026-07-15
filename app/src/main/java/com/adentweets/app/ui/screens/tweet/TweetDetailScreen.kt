package com.adentweets.app.ui.screens.tweet

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.adentweets.app.ui.components.*
import com.adentweets.app.ui.theme.*
import com.adentweets.app.ui.viewmodel.TweetDetailViewModel
import com.adentweets.app.util.Constants
import com.adentweets.app.util.toRelativeTime

@Composable
fun TweetDetailScreen(
    postId: String,
    navController: NavController,
    viewModel: TweetDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(postId) { viewModel.loadTweet(postId) }

    Column(modifier = Modifier.fillMaxSize()) {
        XTopBar(title = "Post", showBack = true, onBack = { navController.popBackStack() })

        if (state.isLoading) {
            LoadingIndicator()
        } else if (state.post == null) {
            EmptyState(icon = Icons.Default.ErrorOutline, title = "Post not found", description = state.error)
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    val p = state.post!!
                    TweetCard(
                        postWithAuthor = p,
                        onLike = { viewModel.toggleLike() },
                        onRepost = { viewModel.repost(p.post.id) },
                        onReply = { pid ->
                            navController.currentBackStackEntry?.savedStateHandle?.set("replyToId", pid)
                            navController.currentBackStackEntry?.savedStateHandle?.set("replyToAuthor", p.author.username)
                            navController.navigate(Constants.Routes.COMPOSE)
                        },
                        onBookmark = { viewModel.toggleBookmark() },
                        onProfileClick = { navController.navigate(Constants.Routes.userProfile(it)) },
                        onTweetClick = {},
                        isDetail = true
                    )
                    Column(modifier = Modifier.padding(horizontal = 56.dp, vertical = 12.dp)) {
                        HorizontalDivider(color = XDarkHover.value, thickness = 1.dp)
                    }
                }

                item {
                    Text(
                        "Replies",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                if (state.replies.isEmpty()) {
                    item {
                        EmptyState(
                            icon = Icons.Outlined.ChatBubbleOutline,
                            title = "Be the first to reply!",
                            actionText = "Reply",
                            onAction = {
                                navController.currentBackStackEntry?.savedStateHandle?.set("replyToId", postId)
                                navController.navigate(Constants.Routes.COMPOSE)
                            }
                        )
                    }
                } else {
                    items(state.replies, key = { it.post.id }) { reply ->
                        TweetCard(
                            postWithAuthor = reply,
                            onLike = {},
                            onRepost = {},
                            onReply = { rpid ->
                                navController.currentBackStackEntry?.savedStateHandle?.set("replyToId", rpid)
                                navController.navigate(Constants.Routes.COMPOSE)
                            },
                            onBookmark = {},
                            onProfileClick = { navController.navigate(Constants.Routes.userProfile(it)) },
                            onTweetClick = { navController.navigate(Constants.Routes.tweetDetail(it)) }
                        )
                    }
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}