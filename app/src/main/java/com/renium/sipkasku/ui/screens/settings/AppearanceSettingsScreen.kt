package com.renium.sipkasku.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.renium.sipkasku.data.repository.SettingsRepository
import kotlinx.coroutines.launch

@Composable
fun AppearanceSettingsScreen(
    settingsRepository: SettingsRepository?
) {
    val scope = rememberCoroutineScope()

    val themeMode by settingsRepository
        ?.getThemeMode()
        ?.collectAsState(initial = "AUTO")
        ?: remember { mutableStateOf("AUTO") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Appearance",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

            RadioButton(
                selected = themeMode == "AUTO",
                onClick = {
                    scope.launch {
                        settingsRepository?.setThemeMode("AUTO")
                    }
                }
            )
            Text("Auto")
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

            RadioButton(
                selected = themeMode == "LIGHT",
                onClick = {
                    scope.launch {
                        settingsRepository?.setThemeMode("LIGHT")
                    }
                }
            )
            Text("Light")
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

            RadioButton(
                selected = themeMode == "DARK",
                onClick = {
                    scope.launch {
                        settingsRepository?.setThemeMode("DARK")
                    }
                }
            )
            Text("Dark")
        }
    }
}
