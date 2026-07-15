package com.adentweets.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adentweets.app.data.model.TrendingTopic
import com.adentweets.app.data.model.User
import com.adentweets.app.domain.repository.SearchRepository
import com.adentweets.app.domain.repository.UserRepository
import com.adentweets.app.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExploreState(
    val searchQuery: String = "",
    val searchResults: List<User> = emptyList(),
    val trendingTopics: List<TrendingTopic> = emptyList(),
    val suggestedUsers: List<User> = emptyList(),
    val isSearching: Boolean = false,
    val error: String = ""
)

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val searchRepository: SearchRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ExploreState())
    val state: StateFlow<ExploreState> = _state

    init {
        loadTrendingTopics()
        loadSuggestedUsers()
    }

    private fun loadTrendingTopics() {
        _state.value = _state.value.copy(
            trendingTopics = listOf(
                TrendingTopic(id = "1", category = "Technology", title = "AdenTweet", tweetCount = "125K", description = "The new social platform"),
                TrendingTopic(id = "2", category = "Trending", title = "#AndroidDev", tweetCount = "45.2K"),
                TrendingTopic(id = "3", category = "Sports", title = "Football", tweetCount = "89.1K"),
                TrendingTopic(id = "4", category = "Entertainment", title = "#Kotlin", tweetCount = "32.5K"),
                TrendingTopic(id = "5", category = "News", title = "Yemen", tweetCount = "15.8K"),
                TrendingTopic(id = "6", category = "Technology", title = "#Compose", tweetCount = "28.3K"),
                TrendingTopic(id = "7", category = "Trending", title = "#Firebase", tweetCount = "19.7K"),
                TrendingTopic(id = "8", category = "Sports", title = "Cricket", tweetCount = "67.4K"),
            )
        )
    }

    private fun loadSuggestedUsers() {
        viewModelScope.launch {
            searchRepository.searchUsers("").onSuccess { users ->
                _state.value = _state.value.copy(suggestedUsers = users.take(5))
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
        if (query.length >= 2) searchUsers(query)
        else _state.value = _state.value.copy(searchResults = emptyList())
    }

    private fun searchUsers(query: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSearching = true)
            when (val result = searchRepository.searchUsers(query)) {
                is Result.Success -> {
                    _state.value = _state.value.copy(searchResults = result.data, isSearching = false)
                }
                is Result.Error -> {
                    _state.value = _state.value.copy(isSearching = false, error = result.message)
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun clearSearch() {
        _state.value = _state.value.copy(searchQuery = "", searchResults = emptyList())
    }
}