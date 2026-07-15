package com.adentweets.app.ui.screens.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.adentweets.app.ui.components.*
import com.adentweets.app.ui.theme.*
import com.adentweets.app.ui.viewmodel.ComposeViewModel
import com.adentweets.app.util.Constants
import kotlinx.coroutines.launch

@Composable
fun ComposeScreen(
    navController: NavController,
    replyToId: String?,
    replyToAuthor: String?,
    composeViewModel: ComposeViewModel = hiltViewModel()
) {
    val state by composeViewModel.state.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(replyToId) {
        if (replyToId != null && replyToAuthor != null) {
            composeViewModel.setReplyTo(replyToId, replyToAuthor)
        }
    }
    LaunchedEffect(state.success) {
        if (state.success) {
            navController.popBackStack()
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(XDark.value)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
            Spacer(Modifier.weight(1f))
            XButton(
                text = "Post",
                onClick = { composeViewModel.postTweet() },
                enabled = composeViewModel.canPost,
                isLoading = state.isLoading
            )
        }

        if (state.replyToId != null) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Replying to @$replyToAuthor",
                    style = MaterialTheme.typography.bodySmall,
                    color = XBlue.value
                )
            }
        }

        Row(modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
            ProfileAvatar(
                avatarUrl = "",
                name = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.displayName ?: "Me",
                size = 40.dp
            )
            Spacer(Modifier.width(12.dp))
            OutlinedTextField(
                value = state.text,
                onValueChange = { composeViewModel.updateText(it) },
                placeholder = { Text("What's happening?", color = XGray.value) },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    cursorColor = XBlue.value
                ),
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
                maxLines = 8
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = { }) { Icon(Icons.Outlined.Image, contentDescription = "Media", tint = XBlue.value) }
                IconButton(onClick = { }) { Icon(Icons.Outlined.GifBox, contentDescription = "GIF", tint = XBlue.value) }
                IconButton(onClick = { }) { Icon(Icons.Outlined.Poll, contentDescription = "Poll", tint = XBlue.value) }
                IconButton(onClick = { }) { Icon(Icons.Outlined.EmojiEmotions, contentDescription = "Emoji", tint = XBlue.value) }
                IconButton(onClick = { }) { Icon(Icons.Outlined.Schedule, contentDescription = "Schedule", tint = XBlue.value) }
                IconButton(onClick = { }) { Icon(Icons.Outlined.LocationOn, contentDescription = "Location", tint = XBlue.value) }
            }
            Text(
                "${state.text.length}/280",
                style = MaterialTheme.typography.labelSmall,
                color = when {
                    state.text.length > 270 -> XRed.value
                    state.text.length > 250 -> XYellow.value
                    else -> XGray.value
                }
            )
        }

        AnimatedVisibility(
            visible = state.mediaUrls.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            LazyRow(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.mediaUrls.size) { index ->
                    Surface(
                        modifier = Modifier.size(100.dp).clip(RoundedCornerShape(12.dp)),
                        color = XDarkElevated.value
                    ) {
                        Box(contentAlignment = Alignment.TopEnd) {
                            IconButton(
                                onClick = { composeViewModel.removeMedia(index) },
                                modifier = Modifier.background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(50))
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )

        if (state.replyToId != null) {
            XButton(
                text = "Reply",
                onClick = { composeViewModel.postTweet() },
                enabled = composeViewModel.canPost,
                isLoading = state.isLoading,
                modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()
            )
        }

        Spacer(Modifier.weight(1f))
        Text(
            "AdenTweet is free. Everyone can post.",
            style = MaterialTheme.typography.bodySmall,
            color = XGray.value,
            modifier = Modifier.padding(16.dp)
        )
    }
}