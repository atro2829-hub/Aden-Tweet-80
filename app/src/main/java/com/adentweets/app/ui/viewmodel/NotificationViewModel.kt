package com.adentweets.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adentweets.app.data.model.AppNotification
import com.adentweets.app.domain.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationState(
    val notifications: List<AppNotification> = emptyList(),
    val unreadCount: Int = 0,
    val isLoading: Boolean = true,
    val selectedTab: Int = 0
)

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _state = MutableStateFlow(NotificationState())
    val state: StateFlow<NotificationState> = _state

    private val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""

    init {
        observeNotifications()
    }

    private fun observeNotifications() {
        viewModelScope.launch {
            notificationRepository.observeNotifications(currentUserId).collect { notifs ->
                val unread = notifs.count { !it.isRead }
                _state.value = _state.value.copy(
                    notifications = notifs,
                    unreadCount = unread,
                    isLoading = false
                )
            }
        }
    }

    fun markAsRead(notifId: String) {
        viewModelScope.launch {
            notificationRepository.markAsRead(currentUserId, notifId)
        }
    }

    fun setTab(tab: Int) { _state.value = _state.value.copy(selectedTab = tab) }
}