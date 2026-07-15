package com.adentweets.app.util

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Exception? = null, val message: String = "") : Result<Nothing>()
    data object Loading : Result<Nothing>()
}

sealed class UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent()
    data class Navigate(val route: String) : UiEvent()
    data object PopBackStack : UiEvent()
}

sealed class AuthError(val message: String) {
    object InvalidEmail : AuthError("invalid_email")
    object UserNotFound : AuthError("user_not_found")
    object WrongPassword : AuthError("wrong_password")
    object WeakPassword : AuthError("weak_password")
    object EmailInUse : AuthError("email_in_use")
    object TooManyRequests : AuthError("too_many_requests")
    object NetworkError : AuthError("network_error")
    object Unknown(val msg: String = "") : AuthError(msg)
}