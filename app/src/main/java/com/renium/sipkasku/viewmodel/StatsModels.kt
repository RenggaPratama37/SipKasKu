package com.renium.sipkasku.viewmodel

// Overview
data class PeriodComparison(
    val currentIncome: Double,
    val currentExpense: Double,
    val previousIncome: Double,
    val previousExpense: Double
) {
    val currentNet get() = currentIncome - currentExpense
    val previousNet get() = previousIncome - previousExpense
    val incomeGrowth get() = if (previousIncome == 0.0) 0.0 else ((currentIncome - previousIncome) / previousIncome) * 100
    val expenseGrowth get() = if (previousExpense == 0.0) 0.0 else ((currentExpense - previousExpense) / previousExpense) * 100
    val netGrowth get() = if (previousNet == 0.0) 0.0 else ((currentNet - previousNet) / kotlin.math.abs(previousNet)) * 100
}

data class TopCategory(
    val categoryId: Int?,
    val categoryName: String,
    val amount: Double,
    val percentage: Double,
    val transactionCount: Int
)

// Trends
data class MonthlySummary(
    val yearMonth: String,
    val income: Double,
    val expense: Double,
    val itemsCount: Int,
    val sortKey: Int
)

data class DailyPoint(
    val dayLabel: String,
    val income: Double,
    val expense: Double
)

data class CashflowPoint(
    val label: String,
    val amount: Double
)

// Categories
data class CategoryBreakdown(
    val categoryId: Int?,
    val categoryName: String,
    val amount: Double,
    val percentage: Double,
    val isIncome: Boolean
)

// Export
data class ExportTransaction(
    val date: String,
    val title: String,
    val categoryName: String,
    val pocketName: String,
    val amount: Double,
    val isIncome: Boolean
)
