package com.renium.sipkasku.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.renium.sipkasku.data.local.Category
import com.renium.sipkasku.data.local.Pocket
import com.renium.sipkasku.data.local.RecurrenceFrequency
import com.renium.sipkasku.data.local.Recurring
import com.renium.sipkasku.data.repository.CategoryRepository
import com.renium.sipkasku.data.repository.PocketRepository
import com.renium.sipkasku.data.repository.RecurringRepository
import com.renium.sipkasku.ui.theme.AppsColors
import com.renium.sipkasku.utils.formatRupiah
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringSettingsScreen(
    navController: NavController,
    recurringRepository: RecurringRepository?,
    categoryRepository: CategoryRepository?,
    pocketRepository: PocketRepository?
) {
    val scope = rememberCoroutineScope()

    // Load data
    val recurrings by recurringRepository?.getAll()?.collectAsState(initial = emptyList())
        ?: remember { mutableStateOf(emptyList()) }
    
    val categories by categoryRepository?.getAll()?.collectAsState(initial = emptyList())
        ?: remember { mutableStateOf(emptyList()) }
    
    val pockets by pocketRepository?.getAllPockets()?.collectAsState(initial = emptyList())
        ?: remember { mutableStateOf(emptyList()) }

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
                onClick = { navController.navigate("add_recurring") },
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

}

@Composable
private fun RecurringPlanCard(
    recurring: Recurring,
    categories: List<Category>,
    pockets: List<Pocket>,
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

    val hasInsufficientBalance = !recurring.isIncome &&
            recurring.lastFailedRun > recurring.lastRun &&
            recurring.lastFailedRun > 0L

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (recurring.isActive)
                MaterialTheme.colorScheme.surfaceContainerLow
            else
                MaterialTheme.colorScheme.surfaceContainerHighest
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
                    color = if (recurring.isIncome) AppsColors.IncomeColor else AppsColors.ExpenseColor
                )
                AssistChip(
                    onClick = {},
                    label = {
                        Text(if (recurring.isIncome) "Income" else "Expense", fontSize = MaterialTheme.typography.labelSmall.fontSize)
                    }
                )
            }

            if (hasInsufficientBalance) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.errorContainer,
                            MaterialTheme.shapes.small
                        )
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        "Insufficient Balance - last execution skipped",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
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
