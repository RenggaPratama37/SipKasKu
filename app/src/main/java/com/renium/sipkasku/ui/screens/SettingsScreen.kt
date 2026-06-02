package com.renium.sipkasku.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Repeat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    settingsRepository: com.renium.sipkasku.data.repository.SettingsRepository? = null
) {

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

        item {
            Card (
                onClick = {
                    navController.navigate("appearance_settings")
                },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                ListItem(
                    leadingContent = {
                        Icon(Icons.Default.Palette, null)
                    },
                    headlineContent = {
                        Text("Appearance", style = MaterialTheme.typography.titleMedium)
                    },
                    supportingContent = {
                        Text("Set app theme mode")
                    }
                )
            }
        }

        item {
            Card (
                onClick = {
                    navController.navigate("Pocket_settings")
                },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                ListItem(
                    leadingContent = {
                        Icon(Icons.Default.AccountBalanceWallet, null)
                    },
                    headlineContent = {
                        Text("Pocket", style = MaterialTheme.typography.titleMedium)
                    },
                    supportingContent = {
                        Text("Set up pockets where money is saved, received, and spent")
                    }
                )
            }
        }

        item {
            Card (
                onClick = {
                    navController.navigate("category_settings")
                },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                ListItem(
                    leadingContent = {
                        Icon(Icons.Default.Category, null)
                    },
                    headlineContent = {
                        Text("Category", style = MaterialTheme.typography.titleMedium)
                    },
                    supportingContent = {
                        Text("Manage income and income categories")
                    }
                )
            }
        }

        item {
            Card (
                onClick = {
                    navController.navigate("recurring_settings")
                },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                ListItem(
                    leadingContent = {
                        Icon(Icons.Default.Repeat, null)
                    },
                    headlineContent = {
                        Text("Systematic Recurring Transaction", style = MaterialTheme.typography.titleMedium)
                    },
                    supportingContent = {
                        Text("Set up systematic income and expense")
                    }
                )
            }
        }

    }
}
