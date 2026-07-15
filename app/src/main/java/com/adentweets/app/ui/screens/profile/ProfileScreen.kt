package com.adentweets.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.adentweets.app.ui.components.*
import com.adentweets.app.ui.theme.*
import com.adentweets.app.ui.viewmodel.FeedViewModel
import com.adentweets.app.ui.viewmodel.ProfileViewModel
import com.adentweets.app.util.Constants
import com.adentweets.app.util.toRelativeTime

@Composable
fun ProfileScreen(
    navController: NavController,
    isOwnProfile: Boolean,
    userId: String = "",
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val state by profileViewModel.state.collectAsState()
    val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val targetUserId = if (isOwnProfile) currentUserId else userId

    LaunchedEffect(targetUserId) {
        if (targetUserId.isNotBlank()) profileViewModel.loadProfile(targetUserId)
    }

    val user = state.user
    val tabTitles = listOf("Posts", "Replies", "Media", "Likes")

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Box {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    color = XDarkHover.value
                ) {
                    if (user.bannerUrl.isNotBlank() && user.bannerUrl.startsWith("data:")) {
                        AsyncImage(
                            model = user.bannerUrl, contentDescription = "Banner",
                            modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxSize().background(XDarkElevated.value))
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, top = 120.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        modifier = Modifier.size(80.dp),
                        shape = CircleShape,
                        color = Color.Black,
                        border = androidx.compose.foundation.BorderStroke(4.dp, Color.Black)
                    ) {
                        ProfileAvatar(avatarUrl = user.avatarUrl, name = user.name, size = 80.dp)
                    }
                    if (isOwnProfile) {
                        Row {
                            IconButton(onClick = { navController.navigate(Constants.Routes.SETTINGS) }) {
                                Icon(Icons.Default.MoreHoriz, contentDescription = "Settings", tint = Color.White)
                            }
                        }
                    } else {
                        XOutlinedButton(
                            text = if (state.isFollowing) "Following" else "Follow",
                            onClick = { profileViewModel.toggleFollow(targetUserId) },
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        user.name.ifBlank { "User" },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (user.isVerified) {
                        Icon(
                            Icons.Default.Verified, contentDescription = "Verified",
                            modifier = Modifier.size(20.dp).padding(start = 4.dp), tint = XBlue.value
                        )
                    }
                }
                Text("@${user.username.ifBlank { "user" }}", style = MaterialTheme.typography.bodyMedium, color = XGray.value)
                if (user.bio.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(user.bio, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (user.location.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp), tint = XGray.value)
                            Spacer(Modifier.width(4.dp))
                            Text(user.location, style = MaterialTheme.typography.bodySmall, color = XGray.value)
                        }
                    }
                    if (user.website.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(16.dp), tint = XBlue.value)
                            Spacer(Modifier.width(4.dp))
                            Text(user.website, style = MaterialTheme.typography.bodySmall, color = XBlue.value)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp), tint = XGray.value)
                        Spacer(Modifier.width(4.dp))
                        Text("Joined ${toFormattedJoinDate(user.createdAt)}", style = MaterialTheme.typography.bodySmall, color = XGray.value)
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row {
                        Text("${user.followingCount}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(Modifier.width(4.dp))
                        Text("Following", style = MaterialTheme.typography.bodyMedium, color = XGray.value)
                    }
                    Row {
                        Text("${user.followersCount}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(Modifier.width(4.dp))
                        Text("Followers", style = MaterialTheme.typography.bodyMedium, color = XGray.value)
                    }
                }
                if (isOwnProfile) {
                    Spacer(Modifier.height(12.dp))
                    XOutlinedButton(
                        text = "Edit profile",
                        onClick = { navController.navigate(Constants.Routes.EDIT_PROFILE) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            TabRow(
                selectedTabIndex = state.selectedTab,
                containerColor = Color.Transparent,
                contentColor = Color.White,
                indicator = { tabPositions ->
                    if (state.selectedTab < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[state.selectedTab]),
                            color = XBlue.value
                        )
                    }
                },
                divider = { HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)) }
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = state.selectedTab == index,
                        onClick = { profileViewModel.setTab(index) },
                        selectedContentColor = Color.White,
                        unselectedContentColor = XGray.value,
                        text = { Text(title, fontWeight = if (state.selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }
        }
        if (state.isLoading && state.posts.isEmpty()) {
            item { LoadingIndicator() }
        } else if (state.posts.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Default.Description,
                    title = "No posts yet"
                )
            }
        } else {
            items(state.posts) { postWithAuthor ->
                TweetCard(
                    postWithAuthor = postWithAuthor,
                    onLike = {},
                    onRepost = {},
                    onReply = { postId ->
                        navController.currentBackStackEntry?.savedStateHandle?.set("replyToId", postId)
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

private fun toFormattedJoinDate(timestamp: Long): String {
    if (timestamp <= 0) return ""
    val sdf = java.text.SimpleDateFormat("MMM yyyy", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}