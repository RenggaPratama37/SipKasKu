package com.renium.sipkasku.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [TransactionEntity::class, Pocket::class, Category::class, Recurring::class],
    version = 7
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao

    abstract fun pocketDao(): PocketDao
    abstract fun categoryDao(): CategoryDao
    abstract fun recurringDao(): RecurringDao
}
