package com.adentweets.app.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.adentweets.app.ui.components.*
import com.adentweets.app.ui.viewmodel.ProfileViewModel
import com.adentweets.app.util.Constants

@Composable
fun FollowersScreen(navController: NavController, userId: String, profileViewModel: ProfileViewModel = hiltViewModel()) {
    val state by profileViewModel.state.collectAsState()
    
    LaunchedEffect(userId) { profileViewModel.loadFollowers(userId) }

    Column(modifier = Modifier.fillMaxSize()) {
        XTopBar(title = "Followers", showBack = true, onBack = { navController.popBackStack() })
        if (state.followers.isEmpty()) {
            EmptyState(icon = androidx.compose.material.icons.Icons.Default.People, title = "No followers yet")
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(state.followers) { user ->
                    UserListItem(
                        user = user,
                        onUserClick = { navController.navigate(Constants.Routes.userProfile(user.id)) }
                    )
                }
            }
        }
    }
}