package com.renium.sipkasku.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Brightness4
import androidx.compose.material.icons.outlined.Brightness6
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
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
            text = "Choose how the application theme is displayed.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.padding(vertical = 8.dp)
            ) {

                ThemeOption(
                    title = "Auto",
                    subtitle = "Follow system settings",
                    selected = themeMode == "AUTO",
                    icon = {
                        Icon(
                            Icons.Outlined.Brightness6,
                            contentDescription = null
                        )
                    }
                ) {
                    scope.launch {
                        settingsRepository?.setThemeMode("AUTO")
                    }
                }

                ThemeOption(
                    title = "Light",
                    subtitle = "Always use light mode",
                    selected = themeMode == "LIGHT",
                    icon = {
                        Icon(
                            Icons.Outlined.LightMode,
                            contentDescription = null
                        )
                    }
                ) {
                    scope.launch {
                        settingsRepository?.setThemeMode("LIGHT")
                    }
                }

                ThemeOption(
                    title = "Dark",
                    subtitle = "Always use dark mode",
                    selected = themeMode == "DARK",
                    icon = {
                        Icon(
                            Icons.Outlined.Brightness4,
                            contentDescription = null
                        )
                    }
                ) {
                    scope.launch {
                        settingsRepository?.setThemeMode("DARK")
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeOption(
    title: String,
    subtitle: String,
    selected: Boolean,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(
                horizontal = 16.dp,
                vertical = 12.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {

        icon()

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        RadioButton(
            selected = selected,
            onClick = null
        )
    }
}
