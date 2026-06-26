package com.renium.sipkasku.ui.components

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
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.renium.sipkasku.ui.screens.MonthSelector
import com.renium.sipkasku.ui.screens.PocketFilter
import com.renium.sipkasku.ui.theme.AppsColors
import com.renium.sipkasku.utils.formatRupiah
import com.renium.sipkasku.viewmodel.StatisticsViewModel
import com.renium.sipkasku.viewmodel.TopCategory

@Composable
fun OverviewTab(viewModel: StatisticsViewModel) {
    val comparison by viewModel.periodComparison.collectAsState()
    val topExpenses by viewModel.topExpenseCategories.collectAsState()
    val topIncomes by viewModel.topIncomeCategories.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val allPockets by viewModel.allPockets.collectAsState()
    val selectedPocketId by viewModel.selectedPocketId.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Month selector
        item {
            MonthSelector(
                selectedMonth = selectedMonth,
                onPrevious = { viewModel.selectMonth(selectedMonth.minusMonths(1)) },
                onNext = { viewModel.selectMonth(selectedMonth.plusMonths(1)) }
            )
        }

        // Pocket filter
        item {
            PocketFilter(
                pockets = allPockets,
                selectedPocketId = selectedPocketId,
                onSelect = { viewModel.selectPocket(it) }
            )
        }

        // Period comparison
        item {
            Text("This Month vs Last Month", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ComparisonCard(
                    modifier = Modifier.weight(1f),
                    label = "Income",
                    current = comparison.currentIncome,
                    growth = comparison.incomeGrowth,
                    isPositiveGood = true
                )
                ComparisonCard(
                    modifier = Modifier.weight(1f),
                    label = "Expense",
                    current = comparison.currentExpense,
                    growth = comparison.expenseGrowth,
                    isPositiveGood = false
                )
            }
            Spacer(Modifier.height(8.dp))
            ComparisonCard(
                modifier = Modifier.fillMaxWidth(),
                label = "Net",
                current = comparison.currentNet,
                growth = comparison.netGrowth,
                isPositiveGood = true
            )
        }

        // Top expense categories
        if (topExpenses.isNotEmpty()) {
            item {
                Text("Top Expenses This Month", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
            }
            items(topExpenses) { cat ->
                TopExpenseRow(cat)
            }
        }
        // Top Income categories
        if (topIncomes.isNotEmpty()) {
            item {
                Text("Top Incomes This Month", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
            }
            items(topIncomes) { cat ->
                TopIncomeRow(cat)
            }
        }

    }
}

@Composable
fun ComparisonCard(
    modifier: Modifier = Modifier,
    label: String,
    current: Double,
    growth: Double,
    isPositiveGood: Boolean
) {
    val growthColor = when {
        growth > 0 && isPositiveGood -> AppsColors.LeafGreen
        growth > 0 && !isPositiveGood -> Color.Red
        growth < 0 && isPositiveGood -> Color.Red
        growth < 0 && !isPositiveGood -> AppsColors.LeafGreen
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val arrow = if (growth >= 0) "↑" else "↓"

    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text(formatRupiah(current), style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(2.dp))
            Text(
                "$arrow ${"%.1f".format(kotlin.math.abs(growth))}%",
                style = MaterialTheme.typography.labelSmall,
                color = growthColor
            )
        }
    }
}

@Composable
fun TopExpenseRow(cat: TopCategory) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(cat.categoryName, style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium)
                Text("${cat.transactionCount} transactions",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { (cat.percentage / 100).toFloat() },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.error
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(formatRupiah(cat.amount), style = MaterialTheme.typography.bodyMedium,
                    color = AppsColors.ExpenseColor, fontWeight = FontWeight.SemiBold)
                Text("${"%.1f".format(cat.percentage)}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun TopIncomeRow(cat: TopCategory) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(cat.categoryName, style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium)
                Text("${cat.transactionCount} transactions",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { (cat.percentage / 100).toFloat() },
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Green
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(formatRupiah(cat.amount), style = MaterialTheme.typography.bodyMedium,
                    color = AppsColors.IncomeColor, fontWeight = FontWeight.SemiBold)
                Text("${"%.1f".format(cat.percentage)}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
