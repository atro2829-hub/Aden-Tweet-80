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
fun FollowingScreen(navController: NavController, userId: String, profileViewModel: ProfileViewModel = hiltViewModel()) {
    val state by profileViewModel.state.collectAsState()

    LaunchedEffect(userId) { profileViewModel.loadFollowing(userId) }

    Column(modifier = Modifier.fillMaxSize()) {
        XTopBar(title = "Following", showBack = true, onBack = { navController.popBackStack() })
        if (state.following.isEmpty()) {
            EmptyState(icon = androidx.compose.material.icons.Icons.Default.PeopleOutline, title = "Not following anyone")
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(state.following) { user ->
                    UserListItem(
                        user = user,
                        onUserClick = { navController.navigate(Constants.Routes.userProfile(user.id)) }
                    )
                }
            }
        }
    }
}