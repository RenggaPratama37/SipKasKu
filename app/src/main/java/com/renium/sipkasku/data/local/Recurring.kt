package com.renium.sipkasku.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class RecurrenceFrequency {
    DAILY,              
    WEEKLY,             
    MONTHLY,            
    SPECIFIC_DAY,       
    END_OF_MONTH        
}

@Entity(tableName = "recurrings")
data class Recurring(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val amount: Double,
    val categoryId: Int? = null,          // FK to categories table
    val pocketId: Int? = null,            // FK to pockets table
    val isIncome: Boolean,
    val frequency: String = RecurrenceFrequency.MONTHLY.name,  
    val dayOfMonth: Int = 1,              // 1-31 for SPECIFIC_DAY, or other choice
    val dayOfWeek: Int? = null,           // 1=Monday, 7=Sunday for WEEKLY
    val isActive: Boolean = true,         // Whether this plan is active
    val lastRun: Long = 0L,               // Last time this recurring was processed
    val createdAt: Long = System.currentTimeMillis()
)
