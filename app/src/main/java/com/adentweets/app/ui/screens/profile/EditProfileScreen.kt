package com.adentweets.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.adentweets.app.ui.components.*
import com.adentweets.app.ui.theme.*
import com.adentweets.app.ui.viewmodel.ProfileViewModel
import com.adentweets.app.util.Constants
import kotlinx.coroutines.launch

@Composable
fun EditProfileScreen(navController: NavController, profileViewModel: ProfileViewModel = hiltViewModel()) {
    val state by profileViewModel.state.collectAsState()
    val user = state.user
    var name by remember { mutableStateOf(user.name) }
    var username by remember { mutableStateOf(user.username) }
    var bio by remember { mutableStateOf(user.bio) }
    var location by remember { mutableStateOf(user.location) }
    var website by remember { mutableStateOf(user.location) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().background(XDark.value)) {
        XTopBar(
            title = "Edit profile",
            showBack = true,
            onBack = { navController.popBackStack() },
            actions = {
                XButton(
                    text = "Save",
                    onClick = {
                        scope.launch {
                            isLoading = true
                            val updates = mutableMapOf<String, Any>(
                                "name" to name,
                                "username" to username,
                                "bio" to bio,
                                "location" to location,
                                "website" to website
                            )
                            // profileViewModel.updateProfile(updates) - handled by repo
                            isLoading = false
                            navController.popBackStack()
                        }
                    },
                    isLoading = isLoading,
                    enabled = name.isNotBlank() && username.isNotBlank()
                )
            }
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Surface(
                    modifier = Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(16.dp)),
                    color = XDarkElevated.value
                ) { }
                IconButton(
                    onClick = { },
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Change banner", tint = Color.White, modifier = Modifier.size(32.dp))
                }
            }
            Box(modifier = Modifier.offset(y = (-30).dp)) {
                Surface(
                    modifier = Modifier.size(80.dp).clip(CircleShape),
                    color = XDarkElevated.value
                ) {
                    ProfileAvatar(avatarUrl = user.avatarUrl, name = user.name, size = 80.dp)
                }
                IconButton(
                    onClick = { },
                    modifier = Modifier.align(Alignment.BottomEnd).offset(x = (-8).dp, y = (-8).dp)
                        .background(XDark.value, CircleShape)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Change avatar", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.height(24.dp))
            XTextField(value = name, onValueChange = { name = it }, label = "Name")
            Spacer(Modifier.height(12.dp))
            XTextField(value = username, onValueChange = { username = it.filter { c -> c.isLetterOrDigit() || c == '_' } }, label = "Username")
            Spacer(Modifier.height(12.dp))
            XTextField(value = bio, onValueChange = { if (it.length <= 160) bio = it }, label = "Bio", isSingleLine = false, placeholder = "Tell us about yourself")
            Text("${bio.length}/160", style = MaterialTheme.typography.bodySmall, color = if (bio.length > 150) XRed.value else XGray.value, modifier = Modifier.padding(start = 16.dp, top = 4.dp))
            Spacer(Modifier.height(12.dp))
            XTextField(value = location, onValueChange = { location = it }, label = "Location")
            Spacer(Modifier.height(12.dp))
            XTextField(value = website, onValueChange = { website = it }, label = "Website")
            Spacer(Modifier.height(32.dp))
        }
    }
}