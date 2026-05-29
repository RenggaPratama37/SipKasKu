package com.renium.sipkasku.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renium.sipkasku.data.local.TransactionEntity
import com.renium.sipkasku.data.repository.TransactionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.combine
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Locale
import com.renium.sipkasku.viewmodel.MonthlySummary
import com.renium.sipkasku.viewmodel.WeeklySummary
import com.renium.sipkasku.viewmodel.CashflowPoint

class StatisticsViewModel(
    repository: TransactionRepository
) : ViewModel() {

    val transactions: StateFlow<List<TransactionEntity>> =
        repository
            .getAllTransactions()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    val totalIncome =
        transactions
            .map { list ->

                list
                    .filter { it.isIncome }
                    .sumOf { it.amount }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                0.0
            )
    val totalExpense =
        transactions
            .map { list ->

                list
                    .filter { !it.isIncome }
                    .sumOf { it.amount }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                0.0
            )

    val monthlySummaries: StateFlow<List<MonthlySummary>> =
        transactions
            .map { list ->
                val zone = ZoneId.systemDefault()
                val fmt = DateTimeFormatter.ofPattern("MMMM yyyy", Locale("id","ID"))

                list.groupBy { tx ->
                    Instant.ofEpochMilli(tx.date).atZone(zone).toLocalDate().withDayOfMonth(1)
                }.toList().sortedByDescending { it.first }
                    .map { (date, items) ->
                        val income = items.filter { it.isIncome }.sumOf { it.amount }
                        val expense = items.filter { !it.isIncome }.sumOf { it.amount }
                        MonthlySummary(
                            yearMonth = date.format(fmt),
                            income = income,
                            expense = expense,
                            itemsCount = items.size
                        )
                    }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    val weeklySummaries: StateFlow<List<WeeklySummary>> =
        transactions
            .map { list ->
                val zone = ZoneId.systemDefault()
                val wf = WeekFields.of(Locale.getDefault())

                list.groupBy { tx ->
                    val ld = Instant.ofEpochMilli(tx.date).atZone(zone).toLocalDate()
                    val week = ld.get(wf.weekOfWeekBasedYear())
                    val year = ld.year
                    Pair(year, week)
                }.toList().sortedByDescending { entry ->
                    val key = entry.first
                    key.first * 100 + key.second
                }.map { entry ->
                        val yrWeek = entry.first
                        val items = entry.second
                        val y = yrWeek.first
                        val w = yrWeek.second
                        val income = items.filter { it.isIncome }.sumOf { it.amount }
                        val expense = items.filter { !it.isIncome }.sumOf { it.amount }
                        WeeklySummary(
                            weekLabel = "Week $w, $y",
                            income = income,
                            expense = expense
                        )
                    }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    val cashflowPoints: StateFlow<List<CashflowPoint>> =
        transactions
            .map { list ->
                val zone = ZoneId.systemDefault()
                val fmt = DateTimeFormatter.ofPattern("MMM", Locale("id","ID"))

                list.groupBy { tx ->
                    val ld = Instant.ofEpochMilli(tx.date).atZone(zone).toLocalDate()
                    ld.withDayOfMonth(1)
                }.toList().sortedBy { it.first }
                    .map { (date, items) ->
                        val net = items.sumOf { if (it.isIncome) it.amount else -it.amount }
                        CashflowPoint(
                            label = date.format(fmt),
                            amount = net
                        )
                    }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

}