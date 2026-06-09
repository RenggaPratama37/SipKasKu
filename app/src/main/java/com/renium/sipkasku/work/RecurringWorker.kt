package com.renium.sipkasku.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.renium.sipkasku.data.local.AppDatabase
import com.renium.sipkasku.data.local.TransactionEntity
import com.renium.sipkasku.data.local.RecurrenceFrequency
import com.renium.sipkasku.data.repository.RecurringRepository
import com.renium.sipkasku.data.repository.TransactionRepository
import com.renium.sipkasku.data.repository.PocketRepository
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

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try {
                    // Add new columns to recurrings table
                    database.execSQL("ALTER TABLE `recurrings` ADD COLUMN `categoryId` INTEGER")
                    database.execSQL("ALTER TABLE `recurrings` ADD COLUMN `pocketId` INTEGER")
                    database.execSQL("ALTER TABLE `recurrings` ADD COLUMN `frequency` TEXT DEFAULT 'MONTHLY'")
                    database.execSQL("ALTER TABLE `recurrings` ADD COLUMN `dayOfWeek` INTEGER")
                    database.execSQL("ALTER TABLE `recurrings` ADD COLUMN `isActive` INTEGER DEFAULT 1")
                    database.execSQL("ALTER TABLE `recurrings` ADD COLUMN `createdAt` INTEGER DEFAULT 0")
                    
                } catch (t: Throwable) {
                }
            }
        }

        val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "money_manager_db")
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
            .build()

        val transactionRepository = TransactionRepository(db.transactionDao())
        val recurringRepository = RecurringRepository(db.recurringDao())
        val pocketRepository = PocketRepository(db.pocketDao())

        try {
            val today = Calendar.getInstance()
            val currentDay = today.get(Calendar.DAY_OF_MONTH)
            val currentDayOfWeek = today.get(Calendar.DAY_OF_WEEK) // 1=Sunday, 2=Monday, etc
            val currentHour = today.get(Calendar.HOUR_OF_DAY)

            val recurrings = recurringRepository.getAll().first()
            
            recurrings.filter { it.isActive }.forEach { r ->
                val lastRunCalendar = if (r.lastRun == 0L) {
                    Calendar.getInstance().apply { timeInMillis = 0 }
                } else {
                    Calendar.getInstance().apply { timeInMillis = r.lastRun }
                }
                
                val shouldRun = when (r.frequency) {
                    RecurrenceFrequency.DAILY.name -> {
                        // Run once per day (check if lastRun was on a different day)
                        val lastRunDay = lastRunCalendar.get(Calendar.DAY_OF_YEAR)
                        val todayDay = today.get(Calendar.DAY_OF_YEAR)
                        lastRunDay != todayDay
                    }
                    
                    RecurrenceFrequency.WEEKLY.name -> {
                        // Run on specified day of week
                        val targetDayOfWeek = r.dayOfWeek ?: 2 // Default Monday if not set
                        val lastRunDayOfWeek = lastRunCalendar.get(Calendar.DAY_OF_WEEK)
                        
                        // Convert: our 1=Monday -> Calendar.DAY_OF_WEEK (1=Sunday, 2=Monday)
                        val calendarDayOfWeek = if (targetDayOfWeek == 7) 1 else (targetDayOfWeek + 1)
                        
                        currentDayOfWeek == calendarDayOfWeek && lastRunDayOfWeek != calendarDayOfWeek
                    }

                    RecurrenceFrequency.MONTHLY.name -> {
                        val lastRunMonth = lastRunCalendar.get(Calendar.MONTH)
                        val lastRunYear = lastRunCalendar.get(Calendar.YEAR)
                        val todayMonth = today.get(Calendar.MONTH)
                        val todayYear = today.get(Calendar.YEAR)

                        currentDay == r.dayOfMonth &&
                                !(lastRunMonth == todayMonth && lastRunYear == todayYear)
                    }
                    
                    RecurrenceFrequency.END_OF_MONTH.name -> {
                        // Run on last day of month
                        val lastDayOfMonth = today.apply { set(Calendar.DATE, 1) }.let {
                            it.add(Calendar.MONTH, 1)
                            it.add(Calendar.DATE, -1)
                            it.get(Calendar.DAY_OF_MONTH)
                        }
                        
                        val lastRunDay = lastRunCalendar.get(Calendar.DAY_OF_MONTH)
                        currentDay == lastDayOfMonth && lastRunDay != lastDayOfMonth
                    }
                    
                    else -> false
                }

                if (shouldRun) {
                    val categoryId = r.categoryId
                    
                    // Create transaction
                    transactionRepository.insertTransaction(
                        TransactionEntity(
                            title = r.title,
                            amount = r.amount,
                            categoryId = categoryId,
                            isIncome = r.isIncome,
                            date = System.currentTimeMillis(),
                            pocketId = r.pocketId
                        )
                    )

                    r.pocketId?.let { pocketId ->
                        val delta = if (r.isIncome) r.amount else -r.amount
                        pocketRepository.adjustBalance(pocketId, delta)
                    }

                    // Update lastRun
                    recurringRepository.update(r.copy(lastRun = System.currentTimeMillis()))
                }
            }
        } catch (t: Throwable) {
            android.util.Log.e("RecurringWorker", "Error processing recurrings", t)
            return Result.failure()
        }

        return Result.success()
    }
}
