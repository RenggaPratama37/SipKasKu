package com.renium.sipkasku.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import com.renium.sipkasku.data.local.Pocket
import com.renium.sipkasku.data.repository.PocketRepository
import com.renium.sipkasku.data.repository.SettingsRepository
import kotlinx.coroutines.launch

@Composable
fun PocketSettingsScreen(
    transactionRepository: com.renium.sipkasku.data.repository.TransactionRepository?,
    pocketRepository: PocketRepository?,
    settingsRepository: SettingsRepository?
) {

    val scope = rememberCoroutineScope()

    val pockets by pocketRepository?.getAllPockets()
        ?.collectAsState(initial = emptyList())
        ?: remember {
            mutableStateOf(emptyList<Pocket>())
        }

    var newPocketName by remember {
        mutableStateOf("")
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item{
            Text (
                text = "Manage Pocket where balance and transaction separated",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))
            pockets.forEach { 
                p -> ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = p.name,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text (
                                text = "Balance: ${p.balance}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = {
                                scope.launch {
                                    transactionRepository?.deleteByPocketId(
                                        p.id
                                    )
                                    pocketRepository?.deletePocket(p)
                                }
                            }
                        ) {
                           Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Pocket",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
            OutlinedTextField(
                value = newPocketName,
                onValueChange = {
                    newPocketName = it
                },
                label = {
                    Text("Pocket Name")
                },
                placeholder = {
                    Text("Example: Cash")
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    if(newPocketName.isNotBlank()) {
                        scope.launch {
                            pocketRepository?.insertPocket(
                                Pocket(name = newPocketName)
                            )
                            newPocketName = ""
                        }
                    }
                },
                enabled = newPocketName.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = 
                        if (newPocketName.isBlank())
                            MaterialTheme.colorScheme.surfaceVariant
                        else
                            MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Add Pocket")
            }
        }
    }
}
