package com.adentweets.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adentweets.app.domain.repository.AuthRepository
import com.adentweets.app.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val error: String = "",
    val successMessage: String = ""
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state

    init {
        _state.value = _state.value.copy(isLoggedIn = authRepository.isLoggedIn)
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = "")
            when (val result = authRepository.signIn(email, password)) {
                is Result.Success -> {
                    _state.value = _state.value.copy(isLoading = false, isLoggedIn = true)
                }
                is Result.Error -> {
                    _state.value = _state.value.copy(isLoading = false, error = result.message)
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun register(name: String, email: String, password: String, username: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = "")
            when (val result = authRepository.signUp(name, email, password, username)) {
                is Result.Success -> {
                    _state.value = _state.value.copy(isLoading = false, isLoggedIn = true, successMessage = "Welcome to AdenTweet!")
                }
                is Result.Error -> {
                    _state.value = _state.value.copy(isLoading = false, error = result.message)
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = "")
            when (val result = authRepository.resetPassword(email)) {
                is Result.Success -> {
                    _state.value = _state.value.copy(isLoading = false, successMessage = "Password reset email sent")
                }
                is Result.Error -> {
                    _state.value = _state.value.copy(isLoading = false, error = result.message)
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun logout() {
        authRepository.signOut()
        _state.value = _state.value.copy(isLoggedIn = false)
    }

    fun clearError() { _state.value = _state.value.copy(error = "") }
    fun clearSuccess() { _state.value = _state.value.copy(successMessage = "") }
}