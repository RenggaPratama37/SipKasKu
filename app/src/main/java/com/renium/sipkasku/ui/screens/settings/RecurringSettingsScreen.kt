package com.renium.sipkasku.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.renium.sipkasku.data.local.Recurring
import com.renium.sipkasku.data.local.RecurrenceFrequency
import com.renium.sipkasku.data.repository.RecurringRepository
import com.renium.sipkasku.data.repository.CategoryRepository
import com.renium.sipkasku.data.repository.PocketRepository
import com.renium.sipkasku.data.repository.SettingsRepository
import com.renium.sipkasku.utils.formatRupiah
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringSettingsScreen(
    recurringRepository: RecurringRepository?,
    categoryRepository: CategoryRepository?,
    pocketRepository: PocketRepository?,
    settingsRepository: SettingsRepository?
) {
    val scope = rememberCoroutineScope()

    // Load data
    val recurrings by recurringRepository?.getAll()?.collectAsState(initial = emptyList())
        ?: remember { mutableStateOf(emptyList<Recurring>()) }
    
    val categories by categoryRepository?.getAll()?.collectAsState(initial = emptyList())
        ?: remember { mutableStateOf(emptyList()) }
    
    val pockets by pocketRepository?.getAllPockets()?.collectAsState(initial = emptyList())
        ?: remember { mutableStateOf(emptyList()) }

    var showAddDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Set up automatic recurring transactions",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("Add New Plan")
            }
        }

        if (recurrings.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "No plans yet",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            "Create your first automatic plan",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(recurrings) { recurring ->
                RecurringPlanCard(
                    recurring = recurring,
                    categories = categories,
                    pockets = pockets,
                    onDelete = {
                        scope.launch {
                            recurringRepository?.delete(recurring)
                        }
                    },
                    onToggle = {
                        scope.launch {
                            recurringRepository?.update(recurring.copy(isActive = !recurring.isActive))
                        }
                    }
                )
            }
        }
    }

    if (showAddDialog) {
        AddRecurringPlanDialog(
            onDismiss = { showAddDialog = false },
            onSave = { plan ->
                scope.launch {
                    recurringRepository?.insert(plan)
                    showAddDialog = false
                }
            },
            categories = categories,
            pockets = pockets
        )
    }
}

@Composable
private fun RecurringPlanCard(
    recurring: Recurring,
    categories: List<com.renium.sipkasku.data.local.Category>,
    pockets: List<com.renium.sipkasku.data.local.Pocket>,
    onDelete: () -> Unit,
    onToggle: () -> Unit
) {
    val categoryName = categories.firstOrNull { it.id == recurring.categoryId }?.name ?: "Other"
    val pocketName = pockets.firstOrNull { it.id == recurring.pocketId }?.name ?: "Default"
    
    val frequencyLabel = when (recurring.frequency) {
        RecurrenceFrequency.DAILY.name -> "Daily"
        RecurrenceFrequency.WEEKLY.name -> "Weekly"
        RecurrenceFrequency.MONTHLY.name -> "Monthly"
        RecurrenceFrequency.SPECIFIC_DAY.name -> "Specific Day ${recurring.dayOfMonth}"
        RecurrenceFrequency.END_OF_MONTH.name -> "End of Month"
        else -> "Custom"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (recurring.isActive)
                MaterialTheme.colorScheme.surfaceContainerHigh
            else
                MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        recurring.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (recurring.isActive)
                            MaterialTheme.colorScheme.onSurface
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Switch(
                    checked = recurring.isActive,
                    onCheckedChange = { onToggle() }
                )
            }

            // Amount & Type
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    formatRupiah(recurring.amount),
                    style = MaterialTheme.typography.headlineSmall,
                    color = if (recurring.isIncome) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                )
                AssistChip(
                    onClick = {},
                    label = {
                        Text(if (recurring.isIncome) "Income" else "Expense", fontSize = MaterialTheme.typography.labelSmall.fontSize)
                    }
                )
            }

            HorizontalDivider()

            // Schedule details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DetailChip(
                    icon = Icons.Default.Schedule,
                    label = frequencyLabel
                )
                DetailChip(
                    icon = Icons.Default.Category,
                    label = categoryName
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DetailChip(
                    icon = Icons.Default.AccountBalanceWallet,
                    label = pocketName
                )
            }

            // Delete button
            IconButton(
                onClick = {
                    onDelete()
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun DetailChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String
) {
    Row(
        modifier = Modifier
            .background(
                MaterialTheme.colorScheme.surfaceContainerLow,
                MaterialTheme.shapes.small
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AddRecurringPlanDialog(
    onDismiss: () -> Unit,
    onSave: (Recurring) -> Unit,
    categories: List<com.renium.sipkasku.data.local.Category>,
    pockets: List<com.renium.sipkasku.data.local.Pocket>
) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var isIncome by remember { mutableStateOf(false) }
    var frequency by remember { mutableStateOf(RecurrenceFrequency.MONTHLY.name) }
    var dayOfMonth by remember { mutableStateOf("1") }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var selectedPocketId by remember { mutableStateOf<Int?>(null) }
    var validationError by remember { mutableStateOf("") }

    val parsedAmount = amount.toDoubleOrNull() ?: 0.0
    val isFormValid = title.isNotBlank() && 
                      parsedAmount > 0 && 
                      selectedCategoryId != null && 
                      selectedPocketId != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Create Systematic Plan")
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    // Title
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Plan Name") },
                        placeholder = { Text("e.g., Monthly Savings") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    // Income/Expense toggle
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = !isIncome,
                            onClick = { isIncome = false },
                            label = { Text("Expense") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = isIncome,
                            onClick = { isIncome = true },
                            label = { Text("Income") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    // Amount
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Amount") },
                        placeholder = { Text("e.g., 1000000") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    Text("Frequency", style = MaterialTheme.typography.labelMedium)
                    FrequencySelector(
                        selected = frequency,
                        onSelect = { frequency = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (frequency == RecurrenceFrequency.SPECIFIC_DAY.name) {
                    item {
                        OutlinedTextField(
                            value = dayOfMonth,
                            onValueChange = {
                                val day = it.toIntOrNull() ?: 1
                                dayOfMonth = day.coerceIn(1, 31).toString()
                            },
                            label = { Text("Day of Month") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }

                item {
                    Text("Category *", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.error)
                    if (categories.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.errorContainer,
                                    MaterialTheme.shapes.small
                                )
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Please create a category first in Category Settings",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                categories.forEach { cat ->
                                    FilterChip(
                                        selected = selectedCategoryId == cat.id,
                                        onClick = { selectedCategoryId = cat.id },
                                        label = { Text(cat.name) }
                                    )
                                }
                            }
                            if (selectedCategoryId == null) {
                                Text(
                                    "Please select a category",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }

                item {
                    Text("Pocket *", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.error)
                    if (pockets.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.errorContainer,
                                    MaterialTheme.shapes.small
                                )
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Please create a pocket first in Pocket Settings",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                pockets.forEach { pocket ->
                                    FilterChip(
                                        selected = selectedPocketId == pocket.id,
                                        onClick = { selectedPocketId = pocket.id },
                                        label = { Text(pocket.name) }
                                    )
                                }
                            }
                            if (selectedPocketId == null) {
                                Text(
                                    "Please select a pocket",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }

                if (validationError.isNotEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.errorContainer,
                                    MaterialTheme.shapes.small
                                )
                                .padding(12.dp)
                        ) {
                            Text(
                                validationError,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    validationError = ""
                    when {
                        title.isBlank() -> validationError = "Plan name is required"
                        parsedAmount <= 0 -> validationError = "Amount must be greater than 0"
                        selectedCategoryId == null -> validationError = "Category is required"
                        selectedPocketId == null -> validationError = "Pocket is required"
                        else -> {
                            val plan = Recurring(
                                title = title,
                                amount = parsedAmount,
                                categoryId = selectedCategoryId,
                                pocketId = selectedPocketId,
                                isIncome = isIncome,
                                frequency = frequency,
                                dayOfMonth = dayOfMonth.toIntOrNull() ?: 1,
                                isActive = true
                            )
                            onSave(plan)
                        }
                    }
                },
                enabled = isFormValid
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun FrequencySelector(
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf(
            RecurrenceFrequency.DAILY.name to "Daily",
            RecurrenceFrequency.WEEKLY.name to "Weekly",
            RecurrenceFrequency.MONTHLY.name to "Monthly",
            RecurrenceFrequency.SPECIFIC_DAY.name to "Specific Day",
            RecurrenceFrequency.END_OF_MONTH.name to "End of Month"
        ).forEach { (freq, label) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selected == freq,
                    onClick = { onSelect(freq) }
                )
                Text(label, modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

