package com.adentweets.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.adentweets.app.ui.components.XButton
import com.adentweets.app.ui.components.XTextField
import com.adentweets.app.ui.theme.*
import com.adentweets.app.ui.viewmodel.AuthViewModel
import com.adentweets.app.util.isValidEmail

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onLoginSuccess: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val state by authViewModel.state.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }

    LaunchedEffect(state.isLoggedIn) {
        if (state.isLoggedIn) onLoginSuccess()
    }
    LaunchedEffect(state.error) {
        if (state.error.contains("password", ignoreCase = true)) {
            passwordError = state.error
        } else if (state.error.contains("user", ignoreCase = true) || state.error.contains("email", ignoreCase = true)) {
            emailError = state.error
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(XDark.value)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(60.dp))
            Text(
                text = "AdenTweet",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Black, letterSpacing = 2.sp
                ),
                color = Color.White
            )
            Spacer(Modifier.height(48.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(XDarkSurface.value)
                    .padding(24.dp)
            ) {
                Text(
                    text = "Sign in to AdenTweet",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(Modifier.height(28.dp))
                XTextField(
                    value = email, onValueChange = {
                        email = it
                        emailError = ""
                        authViewModel.clearError()
                    },
                    label = "Email",
                    placeholder = "example@email.com",
                    isError = emailError.isNotBlank(),
                    errorMessage = emailError
                )
                Spacer(Modifier.height(16.dp))
                XTextField(
                    value = password, onValueChange = {
                        password = it
                        passwordError = ""
                        authViewModel.clearError()
                    },
                    label = "Password",
                    isError = passwordError.isNotBlank(),
                    errorMessage = passwordError,
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle password",
                                tint = XGray.value
                            )
                        }
                    }
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Forgot password?",
                    modifier = Modifier.clickable { onNavigateToForgotPassword() }.align(Alignment.End),
                    color = XBlue.value,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(24.dp))
                XButton(
                    text = "Sign In",
                    onClick = {
                        emailError = if (!email.isValidEmail()) "Please enter a valid email" else ""
                        passwordError = if (password.length < 6) "Password must be at least 6 characters" else ""
                        if (emailError.isBlank() && passwordError.isBlank()) {
                            authViewModel.login(email, password)
                        }
                    },
                    isLoading = state.isLoading,
                    enabled = email.isNotBlank() && password.isNotBlank()
                )
            }
            Spacer(Modifier.height(32.dp))
            Text(
                text = buildAnnotatedString {
                    append("Don't have an account? ")
                    withStyle(SpanStyle(color = XBlue.value, fontWeight = FontWeight.Bold)) {
                        append("Sign up")
                    }
                },
                modifier = Modifier.clickable { onNavigateToRegister() },
                color = XGray.value,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        if (state.error.isNotBlank() && emailError.isBlank() && passwordError.isBlank()) {
            Snackbar(
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp),
                containerColor = XRed.value
            ) { Text(state.error, color = Color.White) }
        }
    }
}