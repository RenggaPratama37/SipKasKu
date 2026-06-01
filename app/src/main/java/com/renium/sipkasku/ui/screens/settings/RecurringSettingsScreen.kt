package com.renium.sipkasku.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
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
import com.renium.sipkasku.data.repository.SettingsRepository
import com.renium.sipkasku.data.repository.RecurringRepository
import kotlinx.coroutines.launch

@Composable
fun RecurringSettingsScreen(
    recurringRepository: RecurringRepository?,
    settingsRepository: SettingsRepository?
){
    val scope = rememberCoroutineScope()

    // recurring
    val recurrings by recurringRepository?.getAll()?.collectAsState(initial = emptyList()) ?: remember { mutableStateOf(emptyList<com.renium.sipkasku.data.local.Recurring>()) }
    var newRecurringTitle by remember { mutableStateOf("") }
    var newRecurringAmount by remember { mutableStateOf("") }
    var newRecurringDay by remember { mutableStateOf(1) }
    var newRecurringIsIncome by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            HorizontalDivider()
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
