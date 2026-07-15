package com.adentweets.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.adentweets.app.ui.components.XTopBar
import com.adentweets.app.ui.theme.*

@Composable
fun AppearanceSettingsScreen(navController: NavController) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()
    val options = listOf("Dark", "Light", "System")

    Column(modifier = Modifier.fillMaxSize()) {
        XTopBar(title = "Appearance", showBack = true, onBack = { navController.popBackStack() })
        SettingsGroup(title = "Theme") {
            options.forEach { option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(option, style = MaterialTheme.typography.bodyLarge, color = Color.White)
                    }
                    RadioButton(
                        selected = (option == "Dark" && state.isDarkTheme) || (option == "Light" && !state.isDarkTheme),
                        onClick = { viewModel.updateTheme(option == "Dark" || option == "System") },
                        colors = RadioButtonDefaults.colors(selectedColor = XBlue.value)
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(start = 16.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            }
        }
        SettingsGroup(title = "Accessibility") {
            SettingsSwitch(title = "Reduce motion", subtitle = "Reduce animations in the app", checked = state.reduceMotion, onCheckedChange = { viewModel.toggleReduceMotion(it) })
        }
    }
}