package com.renium.sipkasku.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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

    var pocketName by remember {
        mutableStateOf("")
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item{
            Text (
                text = "Pocket Settings",
                style = MaterialTheme.typography.headlineSmall
            )
            Text (
                text = "Manage Pocket where balance and transaction separated",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))
            var showDeleteDialog by remember { mutableStateOf(false) }
            var pocketToDelete by remember { mutableStateOf<Pocket?>(null) }
            var transferTargetPocketId by remember { mutableStateOf<Int?>(null) }
            var deleteAction by remember { mutableStateOf("DELETE") } // DELETE or TRANSFER

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
                                pocketToDelete = p
                                transferTargetPocketId = pockets.firstOrNull { it.id != p.id }?.id
                                deleteAction = "DELETE"
                                showDeleteDialog = true
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

            if (showDeleteDialog && pocketToDelete != null) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false; pocketToDelete = null },
                    title = { Text("Delete Pocket") },
                    text = {
                        Column {
                            Text("What do you want to do with transactions in '${pocketToDelete?.name}'?")
                            Spacer(modifier = Modifier.height(8.dp))
                            Row {
                                RadioButton(selected = deleteAction=="DELETE", onClick = { deleteAction = "DELETE" })
                                Text("Delete transactions")
                            }
                            Row {
                                RadioButton(selected = deleteAction=="TRANSFER", onClick = { deleteAction = "TRANSFER" })
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Transfer to another pocket")
                                    if (pockets.filter { it.id != pocketToDelete?.id }.isEmpty()) {
                                        Text("No other pockets available", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    } else {
                                        // dropdown of target pockets
                                        val targets = pockets.filter { it.id != pocketToDelete?.id }
                                        var expanded by remember { mutableStateOf(false) }
                                        OutlinedButton(onClick = { expanded = true }) {
                                            Text(targets.firstOrNull { it.id == transferTargetPocketId }?.name ?: "Select target")
                                        }
                                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                            targets.forEach { t -> DropdownMenuItem(text = { Text(t.name) }, onClick = { transferTargetPocketId = t.id; expanded = false }) }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            scope.launch {
                                val toDelete = pocketToDelete
                                if (toDelete != null) {
                                    if (deleteAction == "DELETE") {
                                        transactionRepository?.deleteByPocketId(toDelete.id)
                                    } else if (deleteAction == "TRANSFER" && transferTargetPocketId != null) {
                                        transactionRepository?.updatePocketId(toDelete.id, transferTargetPocketId!!)
                                    }
                                    pocketRepository?.deletePocket(toDelete)
                                }
                                showDeleteDialog = false
                                pocketToDelete = null
                            }
                        }) { Text("Confirm") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false; pocketToDelete = null }) { Text("Cancel") }
                    }
                )
            }
            OutlinedTextField(
                value = pocketName,
                onValueChange = {
                    pocketName = it
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
                    if(pocketName.isNotBlank()) {
                        scope.launch {
                            pocketRepository?.insertPocket(
                                Pocket(name = pocketName)
                            )
                            pocketName = ""
                        }
                    }
                },
                enabled = pocketName.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = 
                        if (pocketName.isBlank())
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
