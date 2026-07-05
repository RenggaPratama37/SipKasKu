package com.renium.sipkasku.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renium.sipkasku.data.local.Category
import com.renium.sipkasku.data.local.Pocket
import com.renium.sipkasku.data.local.TransactionEntity
import com.renium.sipkasku.data.repository.CategoryRepository
import com.renium.sipkasku.data.repository.PocketRepository
import com.renium.sipkasku.data.repository.TransactionRepository
import kotlinx.coroutines.flow.*
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class StatisticsViewModel(
    transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository,
    pocketRepository: PocketRepository
) : ViewModel() {

    private val zone = ZoneId.systemDefault()

    // Raw data
    val transactions: StateFlow<List<TransactionEntity>> =
        transactionRepository.getAllTransactions()
            .distinctUntilChanged()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCategories: StateFlow<List<Category>> =
        categoryRepository.getAll()
            .distinctUntilChanged()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPockets: StateFlow<List<Pocket>> =
        pocketRepository.getAllPockets()
            .distinctUntilChanged()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filters
    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    val selectedMonth: StateFlow<YearMonth> = _selectedMonth.asStateFlow()

    private val _selectedPocketId = MutableStateFlow<Int?>(null)
    val selectedPocketId: StateFlow<Int?> = _selectedPocketId.asStateFlow()

    fun selectMonth(month: YearMonth) { _selectedMonth.value = month }
    fun selectPocket(pocketId: Int?) { _selectedPocketId.value = pocketId }

    // Filtered transactions (by month + pocket)
    val filteredTransactions: StateFlow<List<TransactionEntity>> =
        combine(transactions, _selectedMonth, _selectedPocketId) { txList, month, pocketId ->
            txList.filter { tx ->
                val txMonth = Instant.ofEpochMilli(tx.date).atZone(zone).toLocalDate()
                    .let { YearMonth.of(it.year, it.month) }
                val matchMonth = txMonth == month
                val matchPocket = pocketId == null || tx.pocketId == pocketId
                matchMonth && matchPocket
            }
        }
            .distinctUntilChanged()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Overview
    val periodComparison: StateFlow<PeriodComparison> =
        combine(transactions, _selectedMonth) { txList, month ->
            val prevMonth = month.minusMonths(1)
            fun List<TransactionEntity>.forMonth(m: YearMonth) = filter { tx ->
                val txMonth = Instant.ofEpochMilli(tx.date).atZone(zone).toLocalDate()
                    .let { YearMonth.of(it.year, it.month) }
                txMonth == m
            }
            val current = txList.forMonth(month)
            val previous = txList.forMonth(prevMonth)
            PeriodComparison(
                currentIncome = current.filter { it.isIncome }.sumOf { it.amount },
                currentExpense = current.filter { !it.isIncome }.sumOf { it.amount },
                previousIncome = previous.filter { it.isIncome }.sumOf { it.amount },
                previousExpense = previous.filter { !it.isIncome }.sumOf { it.amount }
            )
        }
            .distinctUntilChanged()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PeriodComparison(0.0, 0.0, 0.0, 0.0))

    // Top Expense Categories
    val topExpenseCategories: StateFlow<List<TopCategory>> =
        combine(filteredTransactions, allCategories) { txList, cats ->
            val catMap = cats.associateBy { it.id }
            val expenses = txList.filter { !it.isIncome }
            val total = expenses.sumOf { it.amount }
            expenses.groupBy { it.categoryId }
                .map { (catId, items) ->
                    TopCategory(
                        categoryId = catId,
                        categoryName = catMap[catId]?.name ?: "Uncategorized",
                        amount = items.sumOf { it.amount },
                        percentage = if (total == 0.0) 0.0 else (items.sumOf { it.amount } / total) * 100,
                        transactionCount = items.size
                    )
                }
                .sortedByDescending { it.amount }
                .take(5)
        }
            .distinctUntilChanged()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Top Income Categories
    val topIncomeCategories: StateFlow<List<TopCategory>> =
        combine(filteredTransactions, allCategories) { txList, cats ->
            val catMap = cats.associateBy { it.id }
            val incomes = txList.filter { it.isIncome }
            val total = incomes.sumOf { it.amount }
            incomes.groupBy { it.categoryId }
                .map { (catId, items) ->
                    TopCategory(
                        categoryId = catId,
                        categoryName = catMap[catId]?.name ?: "Uncategorized",
                        amount = items.sumOf { it.amount },
                        percentage = if (total == 0.0) 0.0 else (items.sumOf { it.amount } / total) * 100,
                        transactionCount = items.size
                    )
                }
                .sortedByDescending { it.amount }
                .take(5)
        }
            .distinctUntilChanged()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Trends
    val monthlySummaries: StateFlow<List<MonthlySummary>> =
        transactions.map { txList ->
            val fmt = DateTimeFormatter.ofPattern("MMM yyyy", Locale.forLanguageTag("en-ID"))
            txList.groupBy { tx ->
                Instant.ofEpochMilli(tx.date).atZone(zone).toLocalDate()
                    .let { YearMonth.of(it.year, it.month) }
            }.map { (ym, items) ->
                MonthlySummary(
                    yearMonth = ym.format(fmt),
                    income = items.filter { it.isIncome }.sumOf { it.amount },
                    expense = items.filter { !it.isIncome }.sumOf { it.amount },
                    itemsCount = items.size,
                    sortKey = ym.year * 100 + ym.monthValue
                )
            }
            .filter { it.income > 0 || it.expense > 0 }
            .sortedByDescending { it.sortKey }
        }
            .distinctUntilChanged()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailyPoints: StateFlow<List<DailyPoint>> =
        filteredTransactions.map { txList ->
            val fmt = DateTimeFormatter.ofPattern("dd")
            txList.groupBy { tx ->
                Instant.ofEpochMilli(tx.date).atZone(zone).toLocalDate()
            }.map { (date, items) ->
                DailyPoint(
                    dayLabel = date.format(fmt),
                    income = items.filter { it.isIncome }.sumOf { it.amount },
                    expense = items.filter { !it.isIncome }.sumOf { it.amount }
                )
            }.sortedBy { it.dayLabel }
        }
            .distinctUntilChanged()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cashflowPoints: StateFlow<List<CashflowPoint>> =
        transactions.map { txList ->
            val fmt = DateTimeFormatter.ofPattern("MMM yy", Locale.forLanguageTag("en-ID"))
            txList.groupBy { tx ->
                Instant.ofEpochMilli(tx.date).atZone(zone).toLocalDate()
                    .let { YearMonth.of(it.year, it.month) }
            }.toList().sortedBy { it.first }
                .map { (ym, items) ->
                    CashflowPoint(
                        label = ym.format(fmt),
                        amount = items.sumOf { if (it.isIncome) it.amount else -it.amount }
                    )
                }
        }
            .distinctUntilChanged()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Categories
    val expenseBreakdown: StateFlow<List<CategoryBreakdown>> =
        combine(filteredTransactions, allCategories) { txList, cats ->
            val catMap = cats.associateBy { it.id }
            val expenses = txList.filter { !it.isIncome }
            val total = expenses.sumOf { it.amount }
            expenses.groupBy { it.categoryId }
                .map { (catId, items) ->
                    val amt = items.sumOf { it.amount }
                    CategoryBreakdown(
                        categoryId = catId,
                        categoryName = catMap[catId]?.name ?: "Uncategorized",
                        amount = amt,
                        percentage = if (total == 0.0) 0.0 else (amt / total) * 100,
                        isIncome = false
                    )
                }.sortedByDescending { it.amount }
        }
            .distinctUntilChanged()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val incomeBreakdown: StateFlow<List<CategoryBreakdown>> =
        combine(filteredTransactions, allCategories) { txList, cats ->
            val catMap = cats.associateBy { it.id }
            val incomes = txList.filter { it.isIncome }
            val total = incomes.sumOf { it.amount }
            incomes.groupBy { it.categoryId }
                .map { (catId, items) ->
                    val amt = items.sumOf { it.amount }
                    CategoryBreakdown(
                        categoryId = catId,
                        categoryName = catMap[catId]?.name ?: "Uncategorized",
                        amount = amt,
                        percentage = if (total == 0.0) 0.0 else (amt / total) * 100,
                        isIncome = true
                    )
                }.sortedByDescending { it.amount }
        }
            .distinctUntilChanged()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Export
    val exportTransactions: StateFlow<List<ExportTransaction>> =
        combine(filteredTransactions, allCategories, allPockets) { txList, cats, pockets ->
            val catMap = cats.associateBy { it.id }
            val pocketMap = pockets.associateBy { it.id }
            val fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            txList.sortedByDescending { it.date }.map { tx ->
                ExportTransaction(
                    date = Instant.ofEpochMilli(tx.date).atZone(zone).toLocalDate().format(fmt),
                    title = tx.title,
                    categoryName = catMap[tx.categoryId]?.name ?: "-",
                    pocketName = pocketMap[tx.pocketId]?.name ?: "-",
                    amount = tx.amount,
                    isIncome = tx.isIncome
                )
            }
        }
            .distinctUntilChanged()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
