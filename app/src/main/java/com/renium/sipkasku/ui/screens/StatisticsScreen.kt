package com.renium.sipkasku.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.IconButton
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color

@Composable
fun StatisticsScreen(
    navController: NavController,
    repository: TransactionRepository,
    categoryRepository: com.renium.sipkasku.data.repository.CategoryRepository? = null
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
    val cats = categoryRepository?.getAll()?.collectAsState(initial = emptyList())?.value ?: emptyList()
    val catsMap = remember(cats) { cats.associate { it.id to it.name } }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = {
                    scope.launch {
                            val file = exportTransactionsToCsv(context, viewModel.transactions.value, catsMap)
                            // simple feedback; in-app sharing can be added later
                        }
                }) {
                    Icon(Icons.Default.FileDownload, contentDescription = "Export CSV")
                }
            }
        }

        item {
            // Summary header: Income / Expense / Balance
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Total Income",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = formatRupiah(income),
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Total Expense",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = formatRupiah(expense),
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Current Balance",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = formatRupiah(balance),
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Group by Month", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
        }

        items(monthly) { m ->
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

        item {
            Spacer(modifier = Modifier.height(12.dp))
            Text("Weekly Statistics", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(weekly) { w ->
            WeeklySummaryCard(summary = w)
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
            Text("Cashflow (Monthly)", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            CashflowChart(cashflow = cashflow)
        }

        // End of stats - no additional summary cards here (already shown above)
    }

}

@Composable
private fun WeeklySummaryCard(summary: com.renium.sipkasku.viewmodel.WeeklySummary) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp)) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(summary.weekLabel, style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(4.dp))
                // Weekly model does not include item count currently; show summary subtitle
                Text("Weekly summary", style = MaterialTheme.typography.bodySmall)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    formatRupiah(summary.income),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    formatRupiah(summary.expense),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun CashflowChart(cashflow: List<com.renium.sipkasku.viewmodel.CashflowPoint>) {
    val maxValue = cashflow.maxOfOrNull { kotlin.math.abs(it.amount) }?.coerceAtLeast(1.0) ?: 1.0
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        cashflow.forEach { point ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val fraction = (kotlin.math.abs(point.amount) / maxValue).toFloat().coerceIn(0f, 1f)
                val barColor = if (point.amount >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                Box(modifier = Modifier.size(width = 44.dp, height = 140.dp), contentAlignment = Alignment.BottomCenter) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val barHeight = size.height * fraction
                        val barWidth = size.width * 0.6f
                        val left = (size.width - barWidth) / 2f
                        drawRoundRect(color = barColor, topLeft = androidx.compose.ui.geometry.Offset(left, size.height - barHeight), size = Size(barWidth, barHeight), cornerRadius = CornerRadius(8f, 8f))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(point.label, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(4.dp))
                Text(formatRupiah(point.amount), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
