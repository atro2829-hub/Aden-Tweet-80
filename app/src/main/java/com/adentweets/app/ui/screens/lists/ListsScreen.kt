package com.adentweets.app.ui.screens.lists

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.adentweets.app.data.model.AdenList
import com.adentweets.app.data.remote.FirebaseService
import com.adentweets.app.ui.components.*
import com.adentweets.app.ui.theme.*
import com.adentweets.app.util.Constants
import com.adentweets.app.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListsViewModel @Inject constructor(
    private val firebaseService: FirebaseService
) : ViewModel() {
    private val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private val _lists = MutableStateFlow<List<AdenList>>(emptyList())
    val lists: StateFlow<List<AdenList>> = _lists
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        viewModelScope.launch {
            firebaseService.observeUserLists(currentUserId).collect {
                _lists.value = it
                _isLoading.value = false
            }
        }
    }
}

@Composable
fun ListsScreen(navController: NavController) {
    val viewModel: ListsViewModel = hiltViewModel()
    val lists by viewModel.lists.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        XTopBar(title = "Lists", showBack = true, onBack = { navController.popBackStack() })
        if (isLoading) {
            LoadingIndicator()
        } else if (lists.isEmpty()) {
            EmptyState(icon = Icons.Default.ListAlt, title = "No lists yet")
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(lists, key = { it.id }) { list ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(list.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                        if (list.description.isNotBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text(list.description, style = MaterialTheme.typography.bodySmall, color = XGray.value)
                        }
                        Text("${list.memberCount} members", style = MaterialTheme.typography.bodySmall, color = XGray.value)
                    }
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                }
            }
        }
    }
}