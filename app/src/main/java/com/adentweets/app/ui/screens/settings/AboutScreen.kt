package com.adentweets.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.adentweets.app.ui.components.XTopBar
import com.adentweets.app.ui.theme.XBlue

@Composable
fun AboutScreen(navController: NavController) {
    Column(modifier = Modifier.fillMaxSize()) {
        XTopBar(title = "About", showBack = true, onBack = { navController.popBackStack() })
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("X", style = MaterialTheme.typography.displayLarge.copy(fontSize = 64.sp, fontWeight = FontWeight.Black), color = Color.White)
            Spacer(Modifier.height(8.dp))
            Text("AdenTweet", style = MaterialTheme.typography.titleMedium, color = XBlue.value, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("Version 2.5.0 (Build 80)", style = MaterialTheme.typography.bodyMedium, color = com.adentweets.app.ui.theme.XGray.value)
            Spacer(Modifier.height(32.dp))
            SettingsItem(icon = androidx.compose.material.icons.Icons.Default.Description, title = "Terms of Service") { }
            SettingsItem(icon = androidx.compose.material.icons.Icons.Default.Security, title = "Privacy Policy") { }
            SettingsItem(icon = androidx.compose.material.icons.Icons.Default.Code, title = "Open Source Licenses") { }
            Spacer(Modifier.height(32.dp))
            Text("Made with love in Aden, Yemen", color = com.adentweets.app.ui.theme.XGray.value, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
        }
    }
}