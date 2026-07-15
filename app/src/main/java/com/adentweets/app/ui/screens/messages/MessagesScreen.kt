package com.adentweets.app.ui.screens.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.adentweets.app.ui.components.*
import com.adentweets.app.ui.theme.*
import com.adentweets.app.ui.viewmodel.MessageViewModel
import com.adentweets.app.util.toRelativeTime

@Composable
fun MessagesScreen(navController: NavController) {
    val viewModel: MessageViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Messages",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { }) {
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
            }
            IconButton(onClick = { }) {
                Icon(Icons.Default.Create, contentDescription = "New Message", tint = XBlue.value)
            }
        }
        OutlinedTextField(
            value = "",
            onValueChange = {},
            placeholder = { Text("Search Direct Messages") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                unfocusedContainerColor = XDarkElevated.value,
                cursorColor = XBlue.value
            ),
            readOnly = true,
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = XGray.value) }
        )
        Spacer(Modifier.height(8.dp))
        if (state.isLoading) {
            LoadingIndicator()
        } else if (state.conversations.isEmpty()) {
            EmptyState(icon = Icons.Default.MailOutline, title = "No messages yet")
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(state.conversations, key = { it.conversation.id }) { conv ->
                    ConversationItem(
                        conversationWithParticipant = conv,
                        onClick = { viewModel.openConversation(conv.conversation.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun ConversationItem(
    conversationWithParticipant: com.adentweets.app.data.model.ConversationWithParticipant,
    onClick: () -> Unit
) {
    val conv = conversationWithParticipant.conversation
    val participant = conversationWithParticipant.participant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProfileAvatar(avatarUrl = participant.avatarUrl, name = participant.name, size = 56.dp)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    participant.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.weight(1f))
                Text(
                    if (conv.lastMessageTimestamp > 0) conv.lastMessageTimestamp.toRelativeTime() else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = XGray.value
                )
            }
            Text(
                "@${participant.username}",
                style = MaterialTheme.typography.bodySmall,
                color = XGray.value,
                maxLines = 1
            )
            Spacer(Modifier.height(2.dp))
            Text(
                conv.lastMessage.ifBlank { "Start a conversation" },
                style = MaterialTheme.typography.bodyMedium,
                color = if (conv.lastMessage.isNotBlank()) MaterialTheme.colorScheme.onSurface else XGray.value,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
    HorizontalDivider(
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
    )
}

@Composable
fun ChatScreen(
    conversationId: String,
    navController: NavController
) {
    val viewModel: MessageViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    LaunchedEffect(conversationId) {
        viewModel.openConversation(conversationId)
    }
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    state.otherUserName.ifBlank { "Chat" },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        if (state.messages.isEmpty()) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text("No messages yet. Say hello!", color = XGray.value, style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.messages) { message ->
                    val isMe = message.senderId == com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                    ) {
                        Surface(
                            shape = RoundedCornerShape(
                                topStart = 16.dp, topEnd = 16.dp,
                                bottomStart = if (isMe) 16.dp else 4.dp,
                                bottomEnd = if (isMe) 4.dp else 16.dp
                            ),
                            color = if (isMe) XBlue.value else XDarkElevated.value
                        ) {
                            Text(
                                message.text,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { }) {
                Icon(Icons.Default.Image, contentDescription = "Attach", tint = XBlue.value)
            }
            OutlinedTextField(
                value = state.messageText,
                onValueChange = { viewModel.updateMessageText(it) },
                placeholder = { Text("Start a new message") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedBorderColor = XBlue.value,
                    unfocusedContainerColor = XDarkElevated.value,
                    focusedContainerColor = XDarkElevated.value,
                    cursorColor = XBlue.value
                ),
                maxLines = 4
            )
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = { viewModel.sendMessage() },
                enabled = state.messageText.isNotBlank()
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = "Send",
                    tint = if (state.messageText.isNotBlank()) XBlue.value else XGray.value
                )
            }
        }
    }
}