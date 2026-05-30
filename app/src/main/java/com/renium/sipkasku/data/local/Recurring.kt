package com.renium.sipkasku.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recurrings")
data class Recurring(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val amount: Double,
    val category: String,
    val isIncome: Boolean,
    val dayOfMonth: Int,
    val lastRun: Long = 0L
)
