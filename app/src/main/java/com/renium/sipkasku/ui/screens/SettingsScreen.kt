package com.renium.sipkasku.ui.screens

import androidx.compose.foundation.layout.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    pocketRepository: com.renium.sipkasku.data.repository.PocketRepository? = null,
    categoryRepository: com.renium.sipkasku.data.repository.CategoryRepository? = null,
    recurringRepository: com.renium.sipkasku.data.repository.RecurringRepository? = null,
    settingsRepository: com.renium.sipkasku.data.repository.SettingsRepository? = null
) {

    val scope = rememberCoroutineScope()

    // Theme mode
    val themeMode by settingsRepository?.getThemeMode()?.collectAsState(initial = "AUTO") ?: remember { mutableStateOf("AUTO") }

    // pocket list
    val pockets by pocketRepository?.getAllPockets()?.collectAsState(initial = emptyList()) ?: remember { mutableStateOf(emptyList<com.renium.sipkasku.data.local.Pocket>()) }
    var newPocketName by remember { mutableStateOf("") }

    // categories
    val incomeCategories by categoryRepository?.getByType("INCOME")?.collectAsState(initial = emptyList()) ?: remember { mutableStateOf(emptyList<com.renium.sipkasku.data.local.Category>()) }
    val expenseCategories by categoryRepository?.getByType("EXPENSE")?.collectAsState(initial = emptyList()) ?: remember { mutableStateOf(emptyList<com.renium.sipkasku.data.local.Category>()) }
    var newCategoryName by remember { mutableStateOf("") }
    var newCategoryType by remember { mutableStateOf("EXPENSE") }

    // recurring
    val recurrings by recurringRepository?.getAll()?.collectAsState(initial = emptyList()) ?: remember { mutableStateOf(emptyList<com.renium.sipkasku.data.local.Recurring>()) }
    var newRecurringTitle by remember { mutableStateOf("") }
    var newRecurringAmount by remember { mutableStateOf("") }
    var newRecurringDay by remember { mutableStateOf(1) }
    var newRecurringIsIncome by remember { mutableStateOf(false) }

    val pocketMandatory by settingsRepository?.isPocketMandatory()?.collectAsState(initial = false) ?: remember { mutableStateOf(false) }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

        item {
            Text("Appearance", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RadioButton(selected = themeMode == "AUTO", onClick = { scope.launch { settingsRepository?.setThemeMode("AUTO") } })
                Text("Auto")
                Spacer(modifier = Modifier.width(8.dp))
                RadioButton(selected = themeMode == "LIGHT", onClick = { scope.launch { settingsRepository?.setThemeMode("LIGHT") } })
                Text("Light")
                Spacer(modifier = Modifier.width(8.dp))
                RadioButton(selected = themeMode == "DARK", onClick = { scope.launch { settingsRepository?.setThemeMode("DARK") } })
                Text("Dark")
            }
        }

        item {
            Divider()
            Text("Pockets", style = MaterialTheme.typography.titleMedium)
            pockets.forEach { p ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(p.name)
                        Text("Balance: ${p.balance}", style = MaterialTheme.typography.bodySmall)
                    }
                    IconButton(onClick = { scope.launch { pocketRepository?.deletePocket(p) } }) { Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete") }
                }
            }

            OutlinedTextField(value = newPocketName, onValueChange = { newPocketName = it }, label = { Text("New pocket name") }, modifier = Modifier.fillMaxWidth())
            Button(onClick = {
                if (newPocketName.isNotBlank()) {
                    scope.launch { pocketRepository?.insertPocket(com.renium.sipkasku.data.local.Pocket(name = newPocketName)) ; newPocketName = "" }
                }
            }, modifier = Modifier.fillMaxWidth()) { Text("Add Pocket") }

            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = pocketMandatory, onCheckedChange = { checked -> scope.launch { settingsRepository?.setPocketMandatory(checked) } })
                Text("Require pocket for transactions")
            }
        }

        item {
            Divider()
            Text("Categories", style = MaterialTheme.typography.titleMedium)

            Text("Income Categories", style = MaterialTheme.typography.titleSmall)
            incomeCategories.forEach { c ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(c.name, modifier = Modifier.weight(1f))
                    IconButton(onClick = { scope.launch { categoryRepository?.delete(c) } }) { Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete") }
                }
            }

            Text("Expense Categories", style = MaterialTheme.typography.titleSmall)
            expenseCategories.forEach { c ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(c.name, modifier = Modifier.weight(1f))
                    IconButton(onClick = { scope.launch { categoryRepository?.delete(c) } }) { Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete") }
                }
            }

            OutlinedTextField(value = newCategoryName, onValueChange = { newCategoryName = it }, label = { Text("New category name") }, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { newCategoryType = "INCOME" }, colors = ButtonDefaults.buttonColors(containerColor = if (newCategoryType=="INCOME") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)) { Text("Income") }
                Button(onClick = { newCategoryType = "EXPENSE" }, colors = ButtonDefaults.buttonColors(containerColor = if (newCategoryType=="EXPENSE") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)) { Text("Expense") }
            }
            Button(onClick = {
                if (newCategoryName.isNotBlank()) {
                    scope.launch { categoryRepository?.insert(com.renium.sipkasku.data.local.Category(name = newCategoryName, type = newCategoryType)) ; newCategoryName = "" }
                }
            }, modifier = Modifier.fillMaxWidth()) { Text("Add Category") }
        }

        item {
            Divider()
            Text("Scheduled Inputs (Recurring)", style = MaterialTheme.typography.titleMedium)
            recurrings.forEach { r ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(r.title)
                        Text("Day ${r.dayOfMonth} - ${if (r.isIncome) "Income" else "Expense"}", style = MaterialTheme.typography.bodySmall)
                    }
                    IconButton(onClick = { scope.launch { recurringRepository?.delete(r) } }) { Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete") }
                }
            }

            OutlinedTextField(value = newRecurringTitle, onValueChange = { newRecurringTitle = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = newRecurringAmount, onValueChange = { newRecurringAmount = it }, label = { Text("Amount") }, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = newRecurringDay.toString(), onValueChange = { newRecurringDay = it.toIntOrNull() ?: 1 }, label = { Text("DayOfMonth") }, modifier = Modifier.width(120.dp))
                Button(onClick = { newRecurringIsIncome = !newRecurringIsIncome }, modifier = Modifier.align(Alignment.CenterVertically)) { Text(if (newRecurringIsIncome) "Income" else "Expense") }
            }
            Button(onClick = {
                val amt = newRecurringAmount.replace(".", "").toDoubleOrNull() ?: 0.0
                if (newRecurringTitle.isNotBlank() && amt > 0.0) {
                    scope.launch {
                        recurringRepository?.insert(com.renium.sipkasku.data.local.Recurring(title = newRecurringTitle, amount = amt, category = "Other", isIncome = newRecurringIsIncome, dayOfMonth = newRecurringDay))
                        newRecurringTitle = ""
                        newRecurringAmount = ""
                        newRecurringDay = 1
                        newRecurringIsIncome = false
                    }
                }
            }, modifier = Modifier.fillMaxWidth()) { Text("Add Recurring") }
        }
    }
}
