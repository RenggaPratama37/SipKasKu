package com.renium.sipkasku.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val amount: Double,
    val categoryId: Int? = null,
    val isIncome: Boolean,
    val date: Long,
    val pocketId: Int? = null
    val recurringId: Int? = null
)
