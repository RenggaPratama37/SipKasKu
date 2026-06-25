package com.renium.sipkasku.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.renium.sipkasku.ui.screens.MonthSelector
import com.renium.sipkasku.ui.theme.ExpenseColor
import com.renium.sipkasku.ui.theme.IncomeColor
import com.renium.sipkasku.utils.formatRupiah
import com.renium.sipkasku.viewmodel.StatisticsViewModel
import android.graphics.Color as AndroidColor

// Tab 2: Trends
@Composable
fun TrendsTab(viewModel: StatisticsViewModel) {
    val monthlySummaries by viewModel.monthlySummaries.collectAsState()
    val dailyPoints by viewModel.dailyPoints.collectAsState()
    val cashflowPoints by viewModel.cashflowPoints.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()

    val primaryColor = IncomeColor.toArgb()
    val errorColor = ExpenseColor.toArgb()
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val gridColor = MaterialTheme.colorScheme.outlineVariant.toArgb()

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

        // Daily bar chart
        if (dailyPoints.isNotEmpty()) {
            item {
                Text("Daily This Month", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    AndroidView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .padding(8.dp),
                        factory = { ctx ->
                            BarChart(ctx).apply {
                                description.isEnabled = false
                                legend.isEnabled = true
                                setDrawGridBackground(false)
                                setBackgroundColor(AndroidColor.TRANSPARENT)
                                xAxis.position = XAxis.XAxisPosition.BOTTOM
                                xAxis.granularity = 1f
                                xAxis.setDrawGridLines(false)
                                axisLeft.setDrawGridLines(true)
                                axisRight.isEnabled = false
                            }
                        },
                        update = { chart ->
                            chart.xAxis.textColor = textColor
                            chart.axisLeft.textColor = textColor
                            chart.axisLeft.gridColor = gridColor
                            chart.legend.textColor = textColor
                            val labels = dailyPoints.map { it.dayLabel }
                            val incomeEntries = dailyPoints.mapIndexed { i, p ->
                                BarEntry(i.toFloat(), p.income.toFloat())
                            }
                            val expenseEntries = dailyPoints.mapIndexed { i, p ->
                                BarEntry(i.toFloat(), p.expense.toFloat())
                            }
                            val incomeSet = BarDataSet(incomeEntries, "Income").apply {
                                color = primaryColor
                                setDrawValues(false)
                            }
                            val expenseSet = BarDataSet(expenseEntries, "Expense").apply {
                                color = errorColor
                                setDrawValues(false)
                            }
                            val data = BarData(incomeSet, expenseSet).apply {
                                barWidth = 0.35f
                            }
                            chart.data = data
                            chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                            chart.groupBars(0f, 0.1f, 0.05f)
                            chart.invalidate()
                        }
                    )
                }
            }
        }

        // Monthly cashflow line chart
        if (cashflowPoints.isNotEmpty()) {
            item {
                Text("Monthly Cashflow", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    AndroidView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .padding(8.dp),
                        factory = { ctx ->
                            LineChart(ctx).apply {
                                description.isEnabled = false
                                legend.isEnabled = false
                                setDrawGridBackground(false)
                                xAxis.position = XAxis.XAxisPosition.BOTTOM
                                xAxis.granularity = 1f
                                xAxis.setDrawGridLines(false)
                                axisRight.isEnabled = false
                            }
                        },
                        update = { chart ->
                            chart.xAxis.textColor = textColor
                            chart.axisLeft.textColor = textColor
                            chart.axisLeft.gridColor = gridColor
                            val labels = cashflowPoints.map { it.label }
                            val entries = cashflowPoints.mapIndexed { i, p ->
                                Entry(i.toFloat(), p.amount.toFloat())
                            }
                            val dataSet = LineDataSet(entries, "Net Cashflow").apply {
                                color = primaryColor
                                setCircleColor(primaryColor)
                                lineWidth = 2f
                                circleRadius = 4f
                                setDrawValues(false)
                                mode = LineDataSet.Mode.CUBIC_BEZIER
                            }
                            chart.data = LineData(dataSet)
                            chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                            chart.invalidate()
                        }
                    )
                }
            }
        }

        // Monthly summary list
        if (monthlySummaries.isNotEmpty()) {
            item {
                Text("Monthly Summary", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
            }
            items(monthlySummaries) { m ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(m.yearMonth, style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold)
                            Text("${m.itemsCount} transactions",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(formatRupiah(m.income), color = IncomeColor,
                                style = MaterialTheme.typography.bodySmall)
                            Text(formatRupiah(m.expense), color = ExpenseColor,
                                style = MaterialTheme.typography.bodySmall)
                            Text(
                                formatRupiah(m.income - m.expense),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = if (m.income >= m.expense) ExpenseColor else IncomeColor
                            )
                        }
                    }
                }
            }
        }
    }
}
