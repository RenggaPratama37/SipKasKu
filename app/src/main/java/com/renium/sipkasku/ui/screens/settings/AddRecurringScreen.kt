package com.renium.sipkasku.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.renium.sipkasku.data.local.RecurrenceFrequency
import com.renium.sipkasku.data.local.Recurring
import com.renium.sipkasku.data.repository.CategoryRepository
import com.renium.sipkasku.data.repository.PocketRepository
import com.renium.sipkasku.data.repository.RecurringRepository
import com.renium.sipkasku.navigation.Screen
import com.renium.sipkasku.ui.theme.AppsColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecurringScreen(
    navController: NavController,
    recurringRepository: RecurringRepository?,
    categoryRepository: CategoryRepository?,
    pocketRepository: PocketRepository?
) {
    val scope = rememberCoroutineScope()

    val categories by categoryRepository?.getAll()
        ?.collectAsState(initial = emptyList())
        ?: remember { mutableStateOf(emptyList()) }

    val pockets by pocketRepository?.getAllPockets()
        ?.collectAsState(initial = emptyList())
        ?: remember { mutableStateOf(emptyList()) }

    // Form state
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var isIncome by remember { mutableStateOf(false) }
    var frequency by remember { mutableStateOf(RecurrenceFrequency.MONTHLY.name) }
    var dayOfMonth by remember { mutableStateOf("1") }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var selectedPocketId by remember { mutableStateOf<Int?>(null) }
    var showValidation by remember { mutableStateOf(false) }

    val parsedAmount = amount.toDoubleOrNull() ?: 0.0
    val isFormValid = title.isNotBlank() &&
            parsedAmount > 0 &&
            selectedCategoryId != null &&
            selectedPocketId != null

    // Category Filter
    val filteredCategories = categories.filter {
        if (isIncome) it.type == "INCOME" else it.type == "EXPENSE"
    }

    // Reset category when type change
    LaunchedEffect(isIncome) {
        selectedCategoryId = null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Income / Expense toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { isIncome = false },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!isIncome) AppsColors.ExpenseColor
                    else MaterialTheme.colorScheme.surface
                )
            ) {
                Text(
                    "Expense",
                    color = if (!isIncome) Color.White
                    else MaterialTheme.colorScheme.onSurface
                )
            }
            Button(
                onClick = { isIncome = true },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isIncome) AppsColors.IncomeColor
                    else MaterialTheme.colorScheme.surface
                )
            ) {
                Text(
                    "Income",
                    color = if (isIncome) Color.White
                    else MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Plan Name
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Plan Name") },
            placeholder = { Text("eg: Monthly saving") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = showValidation && title.isBlank()
        )
        if (showValidation && title.isBlank()) {
            Text(
                "Please insert plan name", color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall
            )
        }

        // Amount
        OutlinedTextField(
            value = amount,
            onValueChange = { input ->
                if (input.all { it.isDigit() || it == '.' }) amount = input
            },
            label = { Text("Amount") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = showValidation && parsedAmount <= 0
        )
        if (showValidation && parsedAmount <= 0) {
            Text(
                "Amount must be greater than 0", color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall
            )
        }

        // Category
        var categoryExpanded by remember { mutableStateOf(false) }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = {
                    categoryExpanded = !categoryExpanded
                },
                modifier = Modifier.weight(1f)
            ) {
                val selectedCategory =
                    filteredCategories.firstOrNull { it.id == selectedCategoryId }

                OutlinedTextField(
                    value = selectedCategory?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    placeholder = { Text("Choose Category") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(categoryExpanded)
                    },
                    modifier = Modifier
                        .menuAnchor(
                            type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                            enabled = true
                        )
                        .fillMaxWidth(),
                    isError = showValidation && selectedCategoryId == null
                )

                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false },
                    modifier = Modifier.heightIn(max = 180.dp)
                ) {
                    if(filteredCategories.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No Categories yet") },
                            onClick = {
                                categoryExpanded = false
                            }
                        )
                    } else {
                        filteredCategories.forEach { category ->
                            DropdownMenuItem(
                                text = {
                                    Text(category.name)
                                },
                                onClick = {
                                    selectedCategoryId = category.id
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Pocket
        var pocketExpanded by remember { mutableStateOf(false) }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ExposedDropdownMenuBox(
                expanded = pocketExpanded,
                onExpandedChange = { pocketExpanded = !pocketExpanded },
                modifier = Modifier.weight(1f)
            ) {
                val selectedPocket = pockets.firstOrNull { it.id == selectedPocketId }
                OutlinedTextField(
                    value = selectedPocket?.name ?:"",
                    onValueChange = {},
                    readOnly = true,
                    label = {Text("Pocket")},
                    placeholder = {Text("Choose Pocket")},
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(pocketExpanded)
                    },
                    modifier = Modifier
                        .menuAnchor(
                            type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                            enabled = true
                        )
                        .fillMaxWidth(),
                    isError = showValidation && selectedPocketId == null
                )
                ExposedDropdownMenu(
                    expanded = pocketExpanded,
                    onDismissRequest = { pocketExpanded = false },
                    modifier = Modifier.heightIn(max = 180.dp)
                ) {
                    if(pockets.isEmpty()) {
                        DropdownMenuItem(
                            text = {Text("Please create a pocket first")},
                            onClick = { pocketExpanded = false }
                        )
                    } else {
                        pockets.forEach { pocket->
                            DropdownMenuItem(
                                text = { Text(pocket.name) },
                                onClick = {
                                    selectedPocketId = pocket.id
                                    pocketExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Frequency
        Text("Frequency", style = MaterialTheme.typography.labelLarge)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(4.dp)) {
                listOf(
                    RecurrenceFrequency.DAILY.name to "Daily",
                    RecurrenceFrequency.WEEKLY.name to "Weekly",
                    RecurrenceFrequency.MONTHLY.name to "Monthly",
                    RecurrenceFrequency.SPECIFIC_DAY.name to "Specific Date",
                    RecurrenceFrequency.END_OF_MONTH.name to "End of Month"
                ).forEach { (freq, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = frequency == freq,
                            onClick = { frequency = freq }
                        )
                        Text(label, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        }

        // Date (Only when specific date)
        if (frequency == RecurrenceFrequency.SPECIFIC_DAY.name) {
            OutlinedTextField(
                value = dayOfMonth,
                onValueChange = {
                    val day = it.toIntOrNull() ?: 1
                    dayOfMonth = day.coerceIn(1, 31).toString()
                },
                label = { Text("Date (1-31)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        // Save button
        Button(
            onClick = {
                if (!isFormValid) {
                    showValidation = true
                    return@Button
                }
                scope.launch {
                    recurringRepository?.insert(
                        Recurring(
                            title = title,
                            amount = parsedAmount,
                            categoryId = selectedCategoryId,
                            pocketId = selectedPocketId,
                            isIncome = isIncome,
                            frequency = frequency,
                            dayOfMonth = dayOfMonth.toIntOrNull() ?: 1,
                            isActive = true
                        )
                    )
                    navController.popBackStack()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isIncome) AppsColors.IncomeColor else AppsColors.ExpenseColor
            )
        ) {
            Text("Save Plan", color = Color.White)
        }
    }
}
