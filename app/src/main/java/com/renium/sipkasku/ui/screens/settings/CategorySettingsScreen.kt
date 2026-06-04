package com.renium.sipkasku.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import com.renium.sipkasku.data.repository.CategoryRepository
import com.renium.sipkasku.data.repository.SettingsRepository
import kotlinx.coroutines.launch

@Composable
fun CategorySettingsScreen(
    transactionRepository: com.renium.sipkasku.data.repository.TransactionRepository?,
    categoryRepository : CategoryRepository?,
    settingsRepository : SettingsRepository?
) {
    val scope = rememberCoroutineScope()

    val incomeCategories by categoryRepository?.getByType("INCOME")?.collectAsState(initial = emptyList()) ?: remember { mutableStateOf(emptyList<com.renium.sipkasku.data.local.Category>()) }
    val expenseCategories by categoryRepository?.getByType("EXPENSE")?.collectAsState(initial = emptyList()) ?: remember { mutableStateOf(emptyList<com.renium.sipkasku.data.local.Category>()) }
    var newCategoryName by remember { mutableStateOf("") }
    var newCategoryType by remember { mutableStateOf("EXPENSE") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {

            Text("Income Categories", style = MaterialTheme.typography.titleSmall)
            incomeCategories.forEach { c ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(c.name, modifier = Modifier.weight(1f))
                    IconButton(onClick = { scope.launch { transactionRepository?.deleteByCategoryId(c.id); categoryRepository?.delete(c) } }) { Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete") }
                }
            }

            Text("Expense Categories", style = MaterialTheme.typography.titleSmall)
            expenseCategories.forEach { c ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(c.name, modifier = Modifier.weight(1f))
                    IconButton(onClick = { scope.launch { transactionRepository?.deleteByCategoryId(c.id); categoryRepository?.delete(c) } }) { Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete") }
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
    }
}
