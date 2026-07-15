package com.adentweets.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adentweets.app.data.model.Post
import com.adentweets.app.domain.repository.PostRepository
import com.adentweets.app.util.Constants
import com.adentweets.app.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ComposeState(
    val text: String = "",
    val mediaUrls: List<String> = emptyList(),
    val mediaType: String? = null,
    val replyToId: String? = null,
    val replyToAuthor: String = "",
    val quotePostId: String? = null,
    val isLoading: Boolean = false,
    val error: String = "",
    val success: Boolean = false
)

@HiltViewModel
class ComposeViewModel @Inject constructor(
    private val postRepository: PostRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ComposeState())
    val state: StateFlow<ComposeState> = _state

    val canPost: Boolean get() = _state.value.text.isNotBlank() && _state.value.text.length <= Constants.TWEET_MAX_LENGTH && !_state.value.isLoading

    fun updateText(text: String) { _state.value = _state.value.copy(text = text) }

    fun setReplyTo(postId: String, authorUsername: String) {
        _state.value = _state.value.copy(replyToId = postId, replyToAuthor = authorUsername)
    }

    fun setQuotePost(postId: String) {
        _state.value = _state.value.copy(quotePostId = postId)
    }

    fun addMedia(base64: String, type: String) {
        if (_state.value.mediaUrls.size < Constants.MAX_MEDIA_ATTACHMENTS) {
            _state.value = _state.value.copy(mediaUrls = _state.value.mediaUrls + base64, mediaType = type)
        }
    }

    fun removeMedia(index: Int) {
        val newList = _state.value.mediaUrls.toMutableList()
        if (index in newList.indices) {
            newList.removeAt(index)
            _state.value = _state.value.copy(
                mediaUrls = newList,
                mediaType = if (newList.isEmpty()) null else _state.value.mediaType
            )
        }
    }

    fun postTweet() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = "")
            when (val result = postRepository.createPost(
                text = _state.value.text,
                mediaUrls = _state.value.mediaUrls,
                mediaType = _state.value.mediaType,
                replyToId = _state.value.replyToId
            )) {
                is Result.Success -> {
                    _state.value = _state.value.copy(isLoading = false, success = true)
                }
                is Result.Error -> {
                    _state.value = _state.value.copy(isLoading = false, error = result.message)
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun clear() {
        _state.value = ComposeState()
    }
}