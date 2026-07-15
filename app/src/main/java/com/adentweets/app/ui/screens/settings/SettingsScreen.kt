package com.adentweets.app.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.adentweets.app.ui.components.XTopBar
import com.adentweets.app.ui.theme.*
import com.adentweets.app.ui.viewmodel.AuthViewModel
import com.adentweets.app.ui.viewmodel.SettingsViewModel
import com.adentweets.app.util.Constants
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(navController: NavController, authViewModel: AuthViewModel) {
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Log out of AdenTweet?", color = Color.White) },
            text = { Text("You can always log back in at any time.", color = XGray.value) },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    authViewModel.logout()
                }) { Text("Log out", color = XRed.value) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel", color = XBlue.value) }
            },
            containerColor = XDarkElevated.value
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        XTopBar(title = "Settings and support", showBack = true, onBack = { navController.popBackStack() })
        SettingsGroup(title = "Your account") {
            SettingsItem(icon = Icons.Default.Person, title = "Account", subtitle = "Account information") {
                navController.navigate(Constants.Routes.ACCOUNT_SETTINGS)
            }
            SettingsItem(icon = Icons.Default.Lock, title = "Privacy and safety", subtitle = "Manage privacy") {
                navController.navigate(Constants.Routes.PRIVACY_SETTINGS)
            }
            SettingsItem(icon = Icons.Default.Notifications, title = "Notifications", subtitle = "Notification preferences") {
                navController.navigate(Constants.Routes.NOTIF_SETTINGS)
            }
        }
        SettingsGroup(title = "General") {
            SettingsItem(icon = Icons.Default.Palette, title = "Appearance", subtitle = "Display, theme") {
                navController.navigate(Constants.Routes.APPEARANCE_SETTINGS)
            }
            SettingsItem(icon = Icons.Default.Language, title = "Language", subtitle = "Arabic") { }
            SettingsItem(icon = Icons.Default.Info, title = "About AdenTweet", subtitle = "Version 2.5.0") {
                navController.navigate(Constants.Routes.ABOUT)
            }
        }
        Spacer(Modifier.weight(1f))
        SettingsItem(
            icon = Icons.Default.Logout,
            title = "Log out",
            subtitle = "Sign out of your account",
            titleColor = XRed.value
        ) { showLogoutDialog = true }
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
        Surface(color = XDarkSurface.value) { Column(content = content) }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String = "",
    titleColor: Color = Color.White,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = XGray.value, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = titleColor)
            if (subtitle.isNotBlank()) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = XGray.value)
            }
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = XGray.value, modifier = Modifier.size(20.dp))
    }
    HorizontalDivider(modifier = Modifier.padding(start = 56.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
}