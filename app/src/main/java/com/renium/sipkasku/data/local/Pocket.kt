package com.renium.sipkasku.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pockets")
data class Pocket(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val balance: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)
