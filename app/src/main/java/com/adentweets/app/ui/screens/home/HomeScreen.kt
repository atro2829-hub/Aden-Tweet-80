package com.adentweets.app.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.adentweets.app.ui.components.*
import com.adentweets.app.ui.screens.notifications.NotificationsScreen
import com.adentweets.app.ui.screens.messages.MessagesScreen
import com.adentweets.app.ui.screens.explore.ExploreScreen
import com.adentweets.app.ui.theme.*
import com.adentweets.app.ui.viewmodel.AuthViewModel
import com.adentweets.app.ui.viewmodel.FeedViewModel
import com.adentweets.app.util.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val feedViewModel: FeedViewModel = hiltViewModel()
    val feedState by feedViewModel.state.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            XBottomNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                onComposeClick = { navController.navigate(Constants.Routes.COMPOSE) },
                onProfileClick = { navController.navigate(Constants.Routes.PROFILE) },
                navController = navController
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Constants.Routes.COMPOSE) },
                containerColor = XBlue.value,
                contentColor = Color.White,
                shape = MaterialTheme.shapes.extraLarge,
                elevation = FloatingActionButtonDefaults.elevation(6.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Compose", tint = Color.White)
            }
        }
    ) { padding ->
        when (selectedTab) {
            0 -> FeedTab(feedViewModel = feedViewModel, feedState = feedState, navController = navController)
            1 -> ExploreScreen(navController = navController)
            2 -> NotificationsScreen(navController = navController)
            3 -> MessagesScreen(navController = navController)
        }
    }
}

@Composable
private fun FeedTab(
    feedViewModel: FeedViewModel,
    feedState: com.adentweets.app.ui.viewmodel.FeedState,
    navController: NavController
) {
    val tabTitles = listOf("For You", "Following")
    var selectedFeedTab by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Home",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }
        TabRow(
            selectedTabIndex = selectedFeedTab,
            containerColor = Color.Transparent,
            contentColor = Color.White,
            indicator = { tabPositions ->
                if (selectedFeedTab < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedFeedTab]),
                        color = XBlue.value
                    )
                }
            },
            divider = {}
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedFeedTab == index,
                    onClick = { selectedFeedTab = index },
                    selectedContentColor = Color.White,
                    unselectedContentColor = XGray.value,
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedFeedTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        if (feedState.isLoading) {
            repeat(5) { ShimmerTweetCard() }
        } else if (feedState.posts.isEmpty()) {
            EmptyState(
                icon = Icons.Outlined.Home,
                title = "Welcome to AdenTweet!",
                description = "Follow people to see their tweets here.",
                actionText = "Find People",
                onAction = { /* navigate to explore */ }
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(feedState.posts, key = { it.post.id }) { postWithAuthor ->
                    TweetCard(
                        postWithAuthor = postWithAuthor,
                        onLike = { feedViewModel.toggleLike(it) },
                        onRepost = { feedViewModel.repost(it) },
                        onReply = { postId ->
                            navController.currentBackStackEntry?.savedStateHandle?.set("replyToId", postId)
                            navController.currentBackStackEntry?.savedStateHandle?.set("replyToAuthor", postWithAuthor.author.username)
                            navController.navigate(Constants.Routes.COMPOSE)
                        },
                        onBookmark = { feedViewModel.toggleBookmark(it) },
                        onProfileClick = { navController.navigate(Constants.Routes.userProfile(it)) },
                        onTweetClick = { navController.navigate(Constants.Routes.tweetDetail(it)) }
                    )
                }
                item {
                    Text(
                        text = "You're all caught up!",
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        color = XGray.value,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun XBottomNavigationBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onComposeClick: () -> Unit,
    onProfileClick: () -> Unit,
    navController: NavController
) {
    val items = listOf(
        Triple(Icons.Outlined.Home, Icons.Filled.Home, "Home"),
        Triple(Icons.Outlined.Search, Icons.Filled.Search, "Explore"),
        Triple(Icons.Outlined.Notifications, Icons.Filled.Notifications, "Notifications"),
        Triple(Icons.Outlined.MailOutline, Icons.Filled.Mail, "Messages")
    )
    NavigationBar(
        containerColor = Color.Black,
        contentColor = Color.White,
        tonalElevation = 0.dp
    ) {
        items.forEachIndexed { index, (outlinedIcon, filledIcon, label) ->
            NavigationBarItem(
                icon = {
                    Icon(
                        if (selectedTab == index) filledIcon else outlinedIcon,
                        contentDescription = label,
                        tint = if (selectedTab == index) Color.White else XGray.value
                    )
                },
                label = {
                    Text(
                        label,
                        color = if (selectedTab == index) Color.White else XGray.value,
                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                    )
                },
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent,
                    selectedIconColor = Color.White,
                    selectedTextColor = Color.White,
                    unselectedIconColor = XGray.value,
                    unselectedTextColor = XGray.value
                )
            )
        }
    }
}