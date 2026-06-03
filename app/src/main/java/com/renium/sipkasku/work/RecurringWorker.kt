package com.renium.sipkasku.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.renium.sipkasku.data.local.AppDatabase
import com.renium.sipkasku.data.local.TransactionEntity
import com.renium.sipkasku.data.repository.RecurringRepository
import com.renium.sipkasku.data.repository.TransactionRepository
import kotlinx.coroutines.flow.first
import java.util.Calendar

class RecurringWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `pockets` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `balance` REAL NOT NULL, `createdAt` INTEGER NOT NULL)"
                )
                try {
                    database.execSQL("ALTER TABLE `transactions` ADD COLUMN `pocketId` INTEGER")
                } catch (t: Throwable) {
                }
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `categories` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `type` TEXT NOT NULL)"
                )
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `recurrings` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `amount` REAL NOT NULL, `category` TEXT NOT NULL, `isIncome` INTEGER NOT NULL, `dayOfMonth` INTEGER NOT NULL, `lastRun` INTEGER NOT NULL)"
                )
            }
        }

        val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "money_manager_db")
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .fallbackToDestructiveMigration()
            .build()

        val transactionRepository = TransactionRepository(db.transactionDao())
        val recurringRepository = RecurringRepository(db.recurringDao())

        try {
            val today = Calendar.getInstance()
            val day = today.get(Calendar.DAY_OF_MONTH)

            val recurrings = recurringRepository.getAll().first()
            recurrings.forEach { r ->
                val lastRunDay = if (r.lastRun == 0L) -1 else Calendar.getInstance().apply { timeInMillis = r.lastRun }.get(Calendar.DAY_OF_MONTH)
                if (r.dayOfMonth == day && lastRunDay != day) {
                    // map recurring.category (name) -> categoryId if exists
                    val cat = db.categoryDao().getByName(r.category)
                    val categoryId = cat?.id
                    transactionRepository.insertTransaction(
                        TransactionEntity(
                            title = r.title,
                            amount = r.amount,
                            categoryId = categoryId,
                            isIncome = r.isIncome,
                            date = System.currentTimeMillis()
                        )
                    )
                    recurringRepository.update(r.copy(lastRun = System.currentTimeMillis()))
                }
            }
        } catch (t: Throwable) {
            return Result.failure()
        }

        return Result.success()
    }
}
