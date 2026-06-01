package com.renium.sipkasku.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.HorizontalDivider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    settingsRepository: com.renium.sipkasku.data.repository.SettingsRepository? = null
) {

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

        item {
            HorizontalDivider()
            ListItem(
                headlineContent = {
                    Text("Appearance", style = MaterialTheme.typography.titleMedium)
                },
                supportingContent = {
                    Text("Set App theme mode")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min=72.dp)
                    .clickable {
                    navController.navigate("appearance_settings")
                }
            )
            HorizontalDivider()
        }

        item {
            HorizontalDivider()
            ListItem(
                headlineContent = {
                    Text("Pocket")
                },
                supportingContent = {
                    Text("Set up pockets where money is saved, received, and spent")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min=72.dp)
                    .clickable {
                        navController.navigate("pocket_settings")
                    }
            )
            HorizontalDivider()
        }

        item {
            HorizontalDivider()
            ListItem(
                headlineContent = {
                    Text("Category")
                },
                supportingContent = {
                    Text("Manage income and expense categories")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min=72.dp)
                    .clickable {
                        navController.navigate("category_settings")
                    }
            )
            HorizontalDivider()
        }

        item {
            HorizontalDivider()
            ListItem(
                headlineContent = {
                    Text("Recurring")
                },
                supportingContent = {
                    Text("Set up systematic income and expense")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min=72.dp)
                    .clickable {
                        navController.navigate("recurring_settings")
                    }
            )
            HorizontalDivider()
        }

    }
}
