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
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.adentweets.app.ui.components.XButton
import com.adentweets.app.ui.components.XTextField
import com.adentweets.app.ui.theme.*
import com.adentweets.app.ui.viewmodel.AuthViewModel
import com.adentweets.app.util.isValidEmail
import com.adentweets.app.util.isValidUsername
import com.adentweets.app.util.isStrongPassword

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val state by authViewModel.state.collectAsState()
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var usernameError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }

    LaunchedEffect(state.isLoggedIn) { if (state.isLoggedIn) onRegisterSuccess() }

    val passwordStrength = when {
        password.isEmpty() -> 0
        password.length < 6 -> 1
        password.isStrongPassword() -> 3
        else -> 2
    }
    val strengthColor = when (passwordStrength) {
        0 -> Color.Transparent
        1 -> XRed.value
        2 -> XYellow.value
        else -> XGreen.value
    }
    val strengthLabel = when (passwordStrength) {
        1 -> "Weak"
        2 -> "Medium"
        3 -> "Strong"
        else -> ""
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(XDark.value)
            .padding(horizontal = 24.dp)
            .padding(top = 16.dp, bottom = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Create your account",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(Modifier.height(24.dp))
            XTextField(
                value = name, onValueChange = { name = it; nameError = ""; authViewModel.clearError() },
                label = "Full Name", placeholder = "Your name",
                isError = nameError.isNotBlank(), errorMessage = nameError
            )
            Spacer(Modifier.height(12.dp))
            XTextField(
                value = email, onValueChange = { email = it; emailError = ""; authViewModel.clearError() },
                label = "Email", placeholder = "example@email.com",
                isError = emailError.isNotBlank(), errorMessage = emailError
            )
            Spacer(Modifier.height(12.dp))
            XTextField(
                value = username, onValueChange = {
                    val filtered = it.filter { c -> c.isLetterOrDigit() || c == '_' }
                    username = filtered
                    usernameError = ""
                    authViewModel.clearError()
                },
                label = "Username", placeholder = "username",
                isError = usernameError.isNotBlank(), errorMessage = usernameError
            )
            Spacer(Modifier.height(12.dp))
            XTextField(
                value = password, onValueChange = { password = it; passwordError = ""; authViewModel.clearError() },
                label = "Password", isError = passwordError.isNotBlank(), errorMessage = passwordError,
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null, tint = XGray.value
                        )
                    }
                }
            )
            if (password.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(3) { i ->
                        Box(
                            modifier = Modifier
                                .width(80.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (i < passwordStrength) strengthColor else XDarkHover.value)
                        )
                        if (i < 2) Spacer(Modifier.width(4.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(strengthLabel, color = strengthColor, style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(Modifier.height(12.dp))
            XTextField(
                value = confirmPassword, onValueChange = { confirmPassword = it; passwordError = "" },
                label = "Confirm Password",
                isError = passwordError.contains("match"), errorMessage = if (passwordError.contains("match")) "Passwords do not match" else ""
            )
            Spacer(Modifier.height(28.dp))
            XButton(
                text = "Create Account",
                onClick = {
                    nameError = if (name.length < 2) "Name must be at least 2 characters" else ""
                    emailError = if (!email.isValidEmail()) "Invalid email address" else ""
                    usernameError = if (!username.isValidUsername()) "3-15 characters, letters, numbers, _ only" else ""
                    passwordError = if (!password.isStrongPassword()) "Min 8 chars, 1 uppercase, 1 lowercase, 1 number" else ""
                    if (password != confirmPassword) passwordError = "Passwords do not match"
                    if (nameError.isBlank() && emailError.isBlank() && usernameError.isBlank() && passwordError.isBlank()) {
                        authViewModel.register(name, email, password, username)
                    }
                },
                isLoading = state.isLoading,
                enabled = name.isNotBlank() && email.isNotBlank() && username.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank()
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text = buildAnnotatedString {
                    append("Already have an account? ")
                    withStyle(SpanStyle(color = XBlue.value, fontWeight = FontWeight.Bold)) { append("Sign in") }
                },
                modifier = Modifier.clickable { onNavigateToLogin() },
                color = XGray.value
            )
            if (state.error.isNotBlank() && nameError.isBlank() && emailError.isBlank()) {
                Spacer(Modifier.height(16.dp))
                Text(state.error, color = XRed.value, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}