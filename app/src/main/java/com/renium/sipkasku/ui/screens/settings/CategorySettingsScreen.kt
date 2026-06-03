package com.renium.sipkasku.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.renium.sipkasku.data.local.Category
import com.renium.sipkasku.data.repository.CategoryRepository
import com.renium.sipkasku.data.repository.SettingsRepository
import kotlinx.coroutines.launch

@Composable
fun CategorySettingsScreen(
    transactionRepository: com.renium.sipkasku.data.repository.TransactionRepository?,
    categoryRepository: CategoryRepository?,
    settingsRepository: SettingsRepository?
) {
    val scope = rememberCoroutineScope()

    val incomeCategories by categoryRepository?.getByType("INCOME")?.collectAsState(initial = emptyList())
        ?: remember { mutableStateOf(emptyList<Category>()) }
    val expenseCategories by categoryRepository?.getByType("EXPENSE")?.collectAsState(initial = emptyList())
        ?: remember { mutableStateOf(emptyList<Category>()) }

    var categoryName by remember { mutableStateOf("") }
    var categoryType by remember { mutableStateOf("EXPENSE") }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var categoryToDelete by remember { mutableStateOf<Category?>(null) }
    var transferTargetCategoryId by remember { mutableStateOf<Int?>(null) }
    var deleteAction by remember { mutableStateOf("DELETE") } // DELETE or TRANSFER

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            HorizontalDivider()
            Text("Categories", style = MaterialTheme.typography.titleLarge)

            Spacer(modifier = Modifier.height(8.dp))

            Text("Income Categories", style = MaterialTheme.typography.titleSmall)
            incomeCategories.forEach { c ->
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(c.name, style = MaterialTheme.typography.bodyLarge)
                        }
                        IconButton(onClick = {
                            categoryToDelete = c
                            transferTargetCategoryId = incomeCategories.firstOrNull { it.id != c.id }?.id
                            deleteAction = "DELETE"
                            showDeleteDialog = true
                        }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Category", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("Expense Categories", style = MaterialTheme.typography.titleSmall)
            expenseCategories.forEach { c ->
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(c.name, style = MaterialTheme.typography.bodyLarge)
                        }
                        IconButton(onClick = {
                            categoryToDelete = c
                            transferTargetCategoryId = expenseCategories.firstOrNull { it.id != c.id }?.id
                            deleteAction = "DELETE"
                            showDeleteDialog = true
                        }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Category", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = categoryName,
                onValueChange = { categoryName = it },
                label = { Text("Category name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { categoryType = "INCOME" }, colors = ButtonDefaults.buttonColors()) { Text("Income") }
                Button(onClick = { categoryType = "EXPENSE" }, colors = ButtonDefaults.buttonColors()) { Text("Expense") }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    if (categoryName.isNotBlank()) {
                        scope.launch {
                            categoryRepository?.insert(Category(name = categoryName, type = categoryType))
                            categoryName = ""
                        }
                    }
                },
                enabled = categoryName.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = if (categoryName.isBlank()) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary)
            ) {
                Text("Add Category")
            }
        }
    }

    if (showDeleteDialog && categoryToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false; categoryToDelete = null },
            title = { Text("Delete Category") },
            text = {
                Column {
                    Text("What do you want to do with transactions in '${categoryToDelete?.name}'?")
                    Spacer(modifier = Modifier.height(8.dp))
                    Row {
                        RadioButton(selected = deleteAction == "DELETE", onClick = { deleteAction = "DELETE" })
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Delete transactions")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = deleteAction == "TRANSFER", onClick = { deleteAction = "TRANSFER" })
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Transfer to another category")
                            val targets = if (incomeCategories.any { it.id == categoryToDelete?.id }) {
                                incomeCategories.filter { it.id != categoryToDelete?.id }
                            } else {
                                expenseCategories.filter { it.id != categoryToDelete?.id }
                            }
                            if (targets.isEmpty()) {
                                Text("No other categories available", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            } else {
                                var expanded by remember { mutableStateOf(false) }
                                OutlinedButton(onClick = { expanded = true }) {
                                    Text(targets.firstOrNull { it.id == transferTargetCategoryId }?.name ?: "Select target")
                                }
                                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                    targets.forEach { t ->
                                        DropdownMenuItem(text = { Text(t.name) }, onClick = { transferTargetCategoryId = t.id; expanded = false })
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        val toDelete = categoryToDelete
                        if (toDelete != null) {
                            if (deleteAction == "DELETE") {
                                transactionRepository?.deleteByCategoryId(toDelete.id)
                            } else if (deleteAction == "TRANSFER" && transferTargetCategoryId != null) {
                                transactionRepository?.updateCategoryId(toDelete.id, transferTargetCategoryId!!)
                            }
                            categoryRepository?.delete(toDelete)
                        }
                        showDeleteDialog = false
                        categoryToDelete = null
                        transferTargetCategoryId = null
                    }
                }) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false; categoryToDelete = null; transferTargetCategoryId = null }) { Text("Cancel") }
            }
        )
    }
}
