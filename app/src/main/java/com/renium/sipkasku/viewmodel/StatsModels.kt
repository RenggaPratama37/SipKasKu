package com.renium.sipkasku.viewmodel

data class MonthlySummary(
    val yearMonth: String,
    val income: Double,
    val expense: Double,
    val itemsCount: Int
)

data class WeeklySummary(
    val weekLabel: String,
    val income: Double,
    val expense: Double
)

data class CashflowPoint(
    val label: String,
    val amount: Double
)
