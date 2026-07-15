package com.adentweets.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adentweets.app.domain.repository.AuthRepository
import com.adentweets.app.data.remote.FirebaseService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsState(
    val isDarkTheme: Boolean = true,
    val language: String = "ar",
    val pushNotifications: Boolean = true,
    val notificationLikes: Boolean = true,
    val notificationReplies: Boolean = true,
    val notificationFollows: Boolean = true,
    val notificationMentions: Boolean = true,
    val notificationReposts: Boolean = true,
    val protectedTweets: Boolean = false,
    val allowMessageRequests: Boolean = true,
    val autoplayMedia: Boolean = true,
    val reduceMotion: Boolean = false,
    val isLoading: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val firebaseService: FirebaseService
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state

    fun logout() { authRepository.logout() }

    fun deleteAccount() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            authRepository.deleteAccount()
            _state.value = _state.value.copy(isLoading = false)
        }
    }

    fun updateTheme(isDark: Boolean) {
        _state.value = _state.value.copy(isDarkTheme = isDark)
    }

    fun updateLanguage(lang: String) {
        _state.value = _state.value.copy(language = lang)
    }

    fun togglePushNotifications(enabled: Boolean) {
        _state.value = _state.value.copy(pushNotifications = enabled)
    }

    fun toggleNotificationLikes(enabled: Boolean) {
        _state.value = _state.value.copy(notificationLikes = enabled)
    }

    fun toggleNotificationReplies(enabled: Boolean) {
        _state.value = _state.value.copy(notificationReplies = enabled)
    }

    fun toggleNotificationFollows(enabled: Boolean) {
        _state.value = _state.value.copy(notificationFollows = enabled)
    }

    fun toggleNotificationMentions(enabled: Boolean) {
        _state.value = _state.value.copy(notificationMentions = enabled)
    }

    fun toggleNotificationReposts(enabled: Boolean) {
        _state.value = _state.value.copy(notificationReposts = enabled)
    }

    fun toggleProtectedTweets(enabled: Boolean) {
        _state.value = _state.value.copy(protectedTweets = enabled)
    }

    fun toggleAllowMessageRequests(enabled: Boolean) {
        _state.value = _state.value.copy(allowMessageRequests = enabled)
    }

    fun toggleAutoplayMedia(enabled: Boolean) {
        _state.value = _state.value.copy(autoplayMedia = enabled)
    }

    fun toggleReduceMotion(enabled: Boolean) {
        _state.value = _state.value.copy(reduceMotion = enabled)
    }
}