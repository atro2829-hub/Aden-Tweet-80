package com.adentweets.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.adentweets.app.ui.components.XButton
import com.adentweets.app.ui.components.XTextField
import com.adentweets.app.ui.theme.*
import com.adentweets.app.ui.viewmodel.AuthViewModel
import com.adentweets.app.util.isValidEmail

@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val state by authViewModel.state.collectAsState()
    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }

    LaunchedEffect(state.successMessage) {
        if (state.successMessage.isNotBlank()) {
            authViewModel.clearSuccess()
            onNavigateBack()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(XDark.value)
            .padding(24.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        Text("Back", color = XBlue.value, style = MaterialTheme.typography.titleMedium,
            modifier = androidx.compose.foundation.clickable { onNavigateBack() })
        Spacer(Modifier.height(32.dp))
        Text("Reset your password", style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(Modifier.height(8.dp))
        Text("Enter your email address and we'll send you a link to reset your password.",
            color = XGray.value, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(32.dp))
        XTextField(
            value = email, onValueChange = { email = it; emailError = "" },
            label = "Email", placeholder = "Your email address",
            isError = emailError.isNotBlank(), errorMessage = emailError
        )
        Spacer(Modifier.height(24.dp))
        XButton(
            text = "Reset Password",
            onClick = {
                emailError = if (!email.isValidEmail()) "Invalid email" else ""
                if (emailError.isBlank()) authViewModel.resetPassword(email)
            },
            isLoading = state.isLoading,
            enabled = email.isNotBlank()
        )
    }
}