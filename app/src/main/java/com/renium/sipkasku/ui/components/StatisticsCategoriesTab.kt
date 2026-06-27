package com.renium.sipkasku.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.renium.sipkasku.ui.screens.MonthSelector
import com.renium.sipkasku.ui.theme.AppsColors
import com.renium.sipkasku.utils.formatRupiah
import com.renium.sipkasku.viewmodel.CategoryBreakdown
import com.renium.sipkasku.viewmodel.StatisticsViewModel
import android.graphics.Color as AndroidColor

// Tab 3: Categories
@Composable
fun CategoriesTab(viewModel: StatisticsViewModel) {
    val expenseBreakdown by viewModel.expenseBreakdown.collectAsState()
    val incomeBreakdown by viewModel.incomeBreakdown.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    var showIncome by remember { mutableStateOf(false) }

    val activeBreakdown = if (showIncome) incomeBreakdown else expenseBreakdown

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            MonthSelector(
                selectedMonth = selectedMonth,
                onPrevious = { viewModel.selectMonth(selectedMonth.minusMonths(1)) },
                onNext = { viewModel.selectMonth(selectedMonth.plusMonths(1)) }
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = !showIncome,
                    onClick = { showIncome = false },
                    label = { Text("Expense") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = showIncome,
                    onClick = { showIncome = true },
                    label = { Text("Income") },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (activeBreakdown.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No data for this period",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            // Pie chart
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    AndroidView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp),
                        factory = { ctx ->
                            PieChart(ctx).apply {
                                description.isEnabled = false
                                isDrawHoleEnabled = true
                                holeRadius = 52f
                                setHoleColor(AndroidColor.TRANSPARENT)
                                setUsePercentValues(true)
                                legend.isEnabled = false
                                setEntryLabelTextSize(11f)
                                setEntryLabelColor(AndroidColor.WHITE)
                            }
                        },
                        update = { chart ->

                            val entries = activeBreakdown.mapIndexed { _, cat ->
                                PieEntry(cat.percentage.toFloat(), cat.categoryName)
                            }
                            val dataSet = PieDataSet(entries, "").apply {
                                colors = AppsColors.chartColors.take(entries.size)
                                sliceSpace = 2f
                                selectionShift = 8f
                            }
                            chart.data = PieData(dataSet).apply {
                                setValueTextSize(0f)
                            }
                            chart.invalidate()
                        }
                    )
                }
            }

            // Category breakdown list
            items(activeBreakdown) { cat ->
                CategoryBreakdownRow(cat)
            }
        }
    }
}

@Composable
private fun CategoryBreakdownRow(cat: CategoryBreakdown) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(cat.categoryName, style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(2.dp))
                LinearProgressIndicator(
                    progress = { (cat.percentage / 100).toFloat() },
                    modifier = Modifier.fillMaxWidth(),
                    color = if (cat.isIncome) AppsColors.IncomeColor else AppsColors.ExpenseColor
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(formatRupiah(cat.amount),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (cat.isIncome) AppsColors.IncomeColor else AppsColors.ExpenseColor)
                Text("${"%.1f".format(cat.percentage)}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
