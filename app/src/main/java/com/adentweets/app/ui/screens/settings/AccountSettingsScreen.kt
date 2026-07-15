package com.adentweets.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.adentweets.app.ui.components.XTopBar
import com.adentweets.app.ui.theme.*
import com.adentweets.app.ui.viewmodel.AuthViewModel
import com.adentweets.app.ui.viewmodel.SettingsViewModel
import com.adentweets.app.util.Constants

@Composable
fun AccountSettingsScreen(navController: NavController, authViewModel: AuthViewModel) {
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    var showDeactivateDialog by remember { mutableStateOf(false) }

    if (showDeactivateDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDeactivateDialog = false },
            title = { androidx.compose.material3.Text("Deactivate account?", color = androidx.compose.ui.graphics.Color.White) },
            text = { androidx.compose.material3.Text("This action cannot be undone.", color = XGray.value) },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    showDeactivateDialog = false
                    settingsViewModel.deleteAccount()
                    authViewModel.logout()
                }) { androidx.compose.material3.Text("Deactivate", color = XRed.value) }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showDeactivateDialog = false }) { androidx.compose.material3.Text("Cancel", color = XBlue.value) }
            },
            containerColor = XDarkElevated.value
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        XTopBar(title = "Account", showBack = true, onBack = { navController.popBackStack() })
        SettingsGroup(title = "Account information") {
            SettingsItem(icon = androidx.compose.material.icons.Icons.Default.Email, title = "Email", subtitle = "Your registered email") { }
            SettingsItem(icon = androidx.compose.material.icons.Icons.Default.Phone, title = "Phone", subtitle = "Not added") { }
        }
        SettingsGroup(title = "Security") {
            SettingsItem(icon = androidx.compose.material.icons.Icons.Default.Password, title = "Change password", subtitle = "Update your password") { }
            SettingsItem(icon = androidx.compose.material.icons.Icons.Default.Shield, title = "Two-factor authentication", subtitle = "Not set up") { }
        }
        SettingsGroup(title = "Data") {
            SettingsItem(icon = androidx.compose.material.icons.Icons.Default.Download, title = "Download your data", subtitle = "Get a copy of your data") { }
        }
        SettingsGroup(title = "Danger zone") {
            SettingsItem(
                icon = androidx.compose.material.icons.Icons.Default.DeleteForever,
                title = "Deactivate account",
                subtitle = "Permanently delete your account",
                titleColor = XRed.value
            ) { showDeactivateDialog = true }
        }
    }