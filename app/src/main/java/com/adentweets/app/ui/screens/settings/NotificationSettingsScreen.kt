package com.adentweets.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.adentweets.app.ui.components.XTopBar
import com.adentweets.app.ui.theme.*
import com.adentweets.app.ui.viewmodel.SettingsViewModel

@Composable
fun NotificationSettingsScreen(navController: NavController) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        XTopBar(title = "Notifications", showBack = true, onBack = { navController.popBackStack() })
        SettingsGroup(title = "Preferences") {
            SettingsSwitch(
                title = "Push notifications",
                subtitle = "Receive push notifications",
                checked = state.pushNotifications,
                onCheckedChange = { viewModel.togglePushNotifications(it) }
            )
            SettingsSwitch(title = "Likes", subtitle = "When someone likes your tweet", checked = state.notificationLikes, onCheckedChange = { viewModel.toggleNotificationLikes(it) })
            SettingsSwitch(title = "Replies", subtitle = "When someone replies to your tweet", checked = state.notificationReplies, onCheckedChange = { viewModel.toggleNotificationReplies(it) })
            SettingsSwitch(title = "Follows", subtitle = "When someone follows you", checked = state.notificationFollows, onCheckedChange = { viewModel.toggleNotificationFollows(it) })
            SettingsSwitch(title = "Mentions", subtitle = "When someone mentions you", checked = state.notificationMentions, onCheckedChange = { viewModel.toggleNotificationMentions(it) })
            SettingsSwitch(title = "Reposts", subtitle = "When someone reposts your tweet", checked = state.notificationReposts, onCheckedChange = { viewModel.toggleNotificationReposts(it) })
        }
    }
}

@Composable
fun SettingsSwitch(title: String, subtitle: String = "", checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = androidx.compose.ui.graphics.Color.White)
            if (subtitle.isNotBlank()) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = XGray.value)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange, colors = SwitchDefaults.colors(checkedTrackColor = XBlue.value, checkedThumbColor = Color.White))
    }
    HorizontalDivider(modifier = Modifier.padding(start = 16.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
}