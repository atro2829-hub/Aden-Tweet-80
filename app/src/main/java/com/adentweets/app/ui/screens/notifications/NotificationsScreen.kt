package com.adentweets.app.ui.screens.notifications

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.adentweets.app.ui.components.*
import com.adentweets.app.ui.theme.*
import com.adentweets.app.ui.viewmodel.NotificationViewModel
import com.adentweets.app.util.Constants
import com.adentweets.app.util.toRelativeTime

@Composable
fun NotificationsScreen(navController: NavController) {
    val viewModel: NotificationViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()
    val tabTitles = listOf("All", "Mentions")

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            "Notifications",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
        )
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
            divider = {}
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = state.selectedTab == index,
                    onClick = { viewModel.setTab(index) },
                    selectedContentColor = Color.White,
                    unselectedContentColor = XGray.value,
                    text = { Text(title, fontWeight = if (state.selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                )
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        if (state.isLoading) {
            LoadingIndicator()
        } else if (state.notifications.isEmpty()) {
            EmptyState(icon = Icons.Default.Notifications, title = "Nothing to see here yet")
        } else {
            val filtered = if (state.selectedTab == 1) state.notifications.filter { it.type == "mention" }
            else state.notifications
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filtered, key = { it.id }) { notif ->
                    NotificationItem(
                        notification = notif,
                        onClick = {
                            viewModel.markAsRead(notif.id)
                            if (notif.targetPostId != null) {
                                navController.navigate(Constants.Routes.tweetDetail(notif.targetPostId))
                            } else {
                                navController.navigate(Constants.Routes.userProfile(notif.actorId))
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationItem(notification: com.adentweets.app.data.model.AppNotification, onClick: () -> Unit) {
    val bgColor = if (!notification.isRead) XDarkElevated.value.copy(alpha = 0.5f) else Color.Transparent
    val icon: ImageVector
    val iconColor: Color
    val actionText: String
    when (notification.type) {
        "like" -> { icon = Icons.Default.Favorite; iconColor = XRed.value; actionText = "liked your tweet" }
        "follow" -> { icon = Icons.Default.PersonAdd; iconColor = XBlue.value; actionText = "followed you" }
        "reply" -> { icon = Icons.Default.ChatBubble; iconColor = XBlue.value; actionText = "replied to your tweet" }
        "repost" -> { icon = Icons.Default.Repeat; iconColor = XGreen.value; actionText = "reposted your tweet" }
        "mention" -> { icon = Icons.Default.AlternateEmail; iconColor = XBlue.value; actionText = "mentioned you" }
        else -> { icon = Icons.Default.Notifications; iconColor = XGray.value; actionText = "" }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.padding(start = 4.dp)) {
            ProfileAvatar(avatarUrl = notification.actorAvatar, name = notification.actorName, size = 40.dp)
            Surface(
                modifier = Modifier.align(Alignment.BottomEnd).size(20.dp),
                shape = CircleShape,
                color = Color.Black
            ) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = iconColor)
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            androidx.compose.foundation.layout.Row {
                Text(
                    text = notification.actorName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.width(4.dp))
                Text(text = actionText, style = MaterialTheme.typography.bodyMedium, color = XGray.value)
            }
            if (notification.targetPostText != null) {
                Text(
                    notification.targetPostText,
                    style = MaterialTheme.typography.bodySmall,
                    color = XLightGray.value,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(notification.createdAt.toRelativeTime(), style = MaterialTheme.typography.bodySmall, color = XGray.value)
        }
        if (!notification.isRead) {
            Box(
                modifier = Modifier.size(10.dp).clip(CircleShape).background(XBlue.value)
            )
        }
    }
    HorizontalDivider(
        modifier = Modifier.padding(start = 68.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
    )
}