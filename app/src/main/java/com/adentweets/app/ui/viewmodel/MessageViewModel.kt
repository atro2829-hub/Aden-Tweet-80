package com.adentweets.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adentweets.app.data.model.ConversationWithParticipant
import com.adentweets.app.data.model.Message
import com.adentweets.app.domain.repository.MessageRepository
import com.adentweets.app.domain.repository.UserRepository
import com.adentweets.app.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MessageState(
    val conversations: List<ConversationWithParticipant> = emptyList(),
    val messages: List<Message> = emptyList(),
    val messageText: String = "",
    val currentConversationId: String = "",
    val otherUserName: String = "",
    val isLoading: Boolean = true,
    val error: String = ""
)

@HiltViewModel
class MessageViewModel @Inject constructor(
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MessageState())
    val state: StateFlow<MessageState> = _state

    private val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""

    init {
        observeConversations()
    }

    private fun observeConversations() {
        viewModelScope.launch {
            messageRepository.observeConversations(currentUserId).collect { convs ->
                val enriched = convs.map { conv ->
                    val otherId = if (conv.conversation.participant1Id == currentUserId)
                        conv.conversation.participant2Id else conv.conversation.participant1Id
                    val userResult = userRepository.getUser(otherId)
                    val user = if (userResult is Result.Success) userResult.data else com.adentweets.app.data.model.User(id = otherId)
                    conv.copy(participant = user)
                }
                _state.value = _state.value.copy(conversations = enriched, isLoading = false)
            }
        }
    }

    fun openConversation(conversationId: String) {
        _state.value = _state.value.copy(currentConversationId = conversationId)
        observeMessages(conversationId)
    }

    private fun observeMessages(conversationId: String) {
        viewModelScope.launch {
            messageRepository.observeMessages(conversationId).collect { msgs ->
                _state.value = _state.value.copy(messages = msgs)
            }
        }
    }

    fun updateMessageText(text: String) {
        _state.value = _state.value.copy(messageText = text)
    }

    fun sendMessage() {
        val text = _state.value.messageText.trim()
        val convId = _state.value.currentConversationId
        if (text.isBlank() || convId.isBlank()) return
        viewModelScope.launch {
            messageRepository.sendMessage(convId, text, null)
            _state.value = _state.value.copy(messageText = "")
        }
    }

    fun startConversation(otherUserId: String) {
        viewModelScope.launch {
            when (val result = messageRepository.getOrCreateConversation(otherUserId)) {
                is Result.Success -> openConversation(result.data)
                else -> Unit
            }
        }
    }
}