package com.renium.sipkasku.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [TransactionEntity::class, Pocket::class, com.renium.sipkasku.data.local.Category::class, com.renium.sipkasku.data.local.Recurring::class],
    version = 5
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao

    abstract fun pocketDao(): PocketDao
    abstract fun categoryDao(): CategoryDao
    abstract fun recurringDao(): RecurringDao
}
