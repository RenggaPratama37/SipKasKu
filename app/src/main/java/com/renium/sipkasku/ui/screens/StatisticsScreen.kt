package com.renium.sipkasku.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.renium.sipkasku.data.repository.TransactionRepository
import com.renium.sipkasku.utils.formatRupiah
import com.renium.sipkasku.viewmodel.StatisticsViewModel
import com.renium.sipkasku.viewmodel.TransactionViewModelFactory
import com.renium.sipkasku.utils.exportTransactionsToCsv
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.progressSemantics
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.IconButton
import androidx.compose.ui.platform.LocalLifecycleOwner

@Composable
fun StatisticsScreen(
    navController: NavController,
    repository: TransactionRepository
) {
    val viewModel: StatisticsViewModel = viewModel(
        factory = TransactionViewModelFactory(repository)
    )
    val income by viewModel
        .totalIncome
        .collectAsState()

    val expense by viewModel
        .totalExpense
        .collectAsState()
    val balance = income - expense
    val monthly by viewModel.monthlySummaries.collectAsState()
    val weekly by viewModel.weeklySummaries.collectAsState()
    val cashflow by viewModel.cashflowPoints.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .padding(16.dp),

        verticalArrangement =
            Arrangement.spacedBy(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            IconButton(onClick = {
                scope.launch {
                    val file = exportTransactionsToCsv(context, viewModel.transactions.value)
                    // simple feedback; in-app sharing can be added later
                }
            }) {
                Icon(Icons.Default.FileDownload, contentDescription = "Export CSV")
            }
        }

        // Summary header: Income / Expense / Balance
        Row(modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(modifier = Modifier.width(140.dp)) {
                Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Income", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = formatRupiah(income), style = MaterialTheme.typography.titleMedium)
                }
            }

            Card(modifier = Modifier.width(140.dp)) {
                Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Expense", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = formatRupiah(expense), style = MaterialTheme.typography.titleMedium)
                }
            }

            Card(modifier = Modifier.width(140.dp)) {
                Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Balance", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = formatRupiah(balance), style = MaterialTheme.typography.titleMedium)
                }
            }
        }

        // Monthly grouped list (Future Feature 1)
        Spacer(modifier = Modifier.height(12.dp))
        Text("Group by Month", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            monthly.forEach { m ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(m.yearMonth, style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Income: ${formatRupiah(m.income)}")
                        Text("Expense: ${formatRupiah(m.expense)}")
                        Text("Items: ${m.itemsCount}")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text("Weekly Statistics", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            weekly.forEach { w ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(w.weekLabel)
                        Column {
                            Text("Income: ${formatRupiah(w.income)}")
                            Text("Expense: ${formatRupiah(w.expense)}")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text("Cashflow (Monthly)", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            cashflow.forEach { point ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val value = point.amount
                    val normalized = (kotlin.math.abs(value) / (cashflow.maxOfOrNull { kotlin.math.abs(it.amount) }?.coerceAtLeast(1.0) ?: 1.0)).toFloat()
                    LinearProgressIndicator(progress = normalized, modifier = Modifier.height(8.dp).width(80.dp))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(point.label)
                    Text(formatRupiah(point.amount))
                }
            }
        }

        // End of stats - no additional summary cards here (already shown above)
    }

}
