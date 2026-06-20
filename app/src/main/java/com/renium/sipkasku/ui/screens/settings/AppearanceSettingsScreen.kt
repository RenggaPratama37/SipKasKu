package com.renium.sipkasku.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Brightness4
import androidx.compose.material.icons.outlined.Brightness6
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
            shape = MaterialTheme.shapes.large,
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
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
