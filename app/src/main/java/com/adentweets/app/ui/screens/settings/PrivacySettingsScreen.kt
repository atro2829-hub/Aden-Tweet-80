package com.adentweets.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.adentweets.app.ui.components.XTopBar
import com.adentweets.app.ui.viewmodel.SettingsViewModel

@Composable
fun PrivacySettingsScreen(navController: NavController) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        XTopBar(title = "Privacy and safety", showBack = true, onBack = { navController.popBackStack() })
        SettingsGroup(title = "Your posts") {
            SettingsSwitch(title = "Protect your posts", subtitle = "Only approved followers can see", checked = state.protectedTweets, onCheckedChange = { viewModel.toggleProtectedTweets(it) })
        }
        SettingsGroup(title = "Direct Messages") {
            SettingsSwitch(title = "Allow message requests", subtitle = "Let people who you don't follow send you messages", checked = state.allowMessageRequests, onCheckedChange = { viewModel.toggleAllowMessageRequests(it) })
        }
        SettingsGroup(title = "Media") {
            SettingsSwitch(title = "Allow photo tagging", subtitle = "Let people tag you in photos", checked = state.allowPhotoTagging, onCheckedChange = { })
            SettingsSwitch(title = "Autoplay media", subtitle = "Autoplay videos and GIFs", checked = state.autoplayMedia, onCheckedChange = { viewModel.toggleAutoplayMedia(it) })
        }
    }
}