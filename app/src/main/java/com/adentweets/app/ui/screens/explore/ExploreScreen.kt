package com.adentweets.app.ui.screens.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Search
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
import com.adentweets.app.ui.viewmodel.ExploreViewModel
import com.adentweets.app.util.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(navController: NavController) {
    val viewModel: ExploreViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()
    var isSearchFocused by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        if (isSearchFocused) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    isSearchFocused = false
                    viewModel.clearSearch()
                }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = { Text("Search AdenTweet") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = XBlue.value,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        unfocusedContainerColor = XDarkElevated.value,
                        focusedContainerColor = XDarkElevated.value,
                        cursorColor = XBlue.value
                    ),
                    singleLine = true
                )
            }
        } else {
            Text(
                text = "Explore",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
            )
            OutlinedTextField(
                value = "",
                onValueChange = {},
                placeholder = { Text("Search AdenTweet") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable { isSearchFocused = true },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    unfocusedContainerColor = XDarkElevated.value,
                    cursorColor = XBlue.value
                ),
                readOnly = true,
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = XGray.value) }
            )
        }

        if (isSearchFocused && state.searchQuery.isNotBlank()) {
            if (state.isSearching) {
                LoadingIndicator()
            } else if (state.searchResults.isEmpty()) {
                EmptyState(icon = Icons.Default.Search, title = "No results found")
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(state.searchResults) { user ->
                        UserListItem(
                            user = user,
                            onUserClick = { navController.navigate(Constants.Routes.userProfile(user.id)) }
                        )
                    }
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    Text(
                        "Trends for you",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
                    )
                }
                items(state.trendingTopics) { topic ->
                    TrendingItem(topic = topic)
                }
                item {
                    Text(
                        "Who to follow",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
                    )
                }
                items(state.suggestedUsers) { user ->
                    UserListItem(
                        user = user,
                        onUserClick = { navController.navigate(Constants.Routes.userProfile(user.id)) }
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun TrendingItem(topic: com.adentweets.app.data.model.TrendingTopic) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(topic.category, style = MaterialTheme.typography.bodySmall, color = XGray.value)
        Spacer(Modifier.height(2.dp))
        Text(
            topic.title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (topic.description != null) {
            Spacer(Modifier.height(2.dp))
            Text(
                topic.description,
                style = MaterialTheme.typography.bodySmall,
                color = XGray.value,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.height(2.dp))
        Text("${topic.tweetCount} posts", style = MaterialTheme.typography.bodySmall, color = XGray.value)
        HorizontalDivider(
            modifier = Modifier.padding(top = 12.dp),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
    }
}