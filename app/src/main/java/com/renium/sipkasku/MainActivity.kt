package com.renium.sipkasku

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.renium.sipkasku.data.local.AppDatabase
import com.renium.sipkasku.data.repository.CategoryRepository
import com.renium.sipkasku.data.repository.PocketRepository
import com.renium.sipkasku.data.repository.RecurringRepository
import com.renium.sipkasku.data.repository.SettingsRepository
import com.renium.sipkasku.data.repository.TransactionRepository
import com.renium.sipkasku.ui.layout.MainScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add pocket table
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `pockets` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `balance` REAL NOT NULL, `createdAt` INTEGER NOT NULL)"
                )

                // Add pocketId column to transactions
                try {
                    database.execSQL("ALTER TABLE `transactions` ADD COLUMN `pocketId` INTEGER")
                } catch (t: Throwable) {
                    // ignore if column exists
                }
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // create categories table
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `categories` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `type` TEXT NOT NULL)"
                )

                // create recurrings table
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `recurrings` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `amount` REAL NOT NULL, `category` TEXT NOT NULL, `isIncome` INTEGER NOT NULL, `dayOfMonth` INTEGER NOT NULL, `lastRun` INTEGER NOT NULL)"
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try {
                    // Add new columns to recurrings table for systematic investment plans
                    database.execSQL("ALTER TABLE `recurrings` ADD COLUMN `categoryId` INTEGER")
                    database.execSQL("ALTER TABLE `recurrings` ADD COLUMN `pocketId` INTEGER")
                    database.execSQL("ALTER TABLE `recurrings` ADD COLUMN `frequency` TEXT DEFAULT 'MONTHLY'")
                    database.execSQL("ALTER TABLE `recurrings` ADD COLUMN `dayOfWeek` INTEGER")
                    database.execSQL("ALTER TABLE `recurrings` ADD COLUMN `isActive` INTEGER DEFAULT 1")
                    database.execSQL("ALTER TABLE `recurrings` ADD COLUMN `createdAt` INTEGER DEFAULT 0")
                } catch (t: Throwable) {
                    // Columns might already exist
                }
            }
        }

        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "money_manager_db"
        ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4).build()

        val transactionRepository = TransactionRepository(
            db.transactionDao()
        )

        val pocketRepository = PocketRepository(
            db.pocketDao()
        )

        val categoryRepository = CategoryRepository(db.categoryDao())
        val recurringRepository = RecurringRepository(db.recurringDao())
        val settingsRepository = SettingsRepository(applicationContext)

        // run simple recurring check on startup: insert transactions for recurrings whose day matches today and haven't run
        // Schedule daily worker to process recurrings reliably
        val periodic = PeriodicWorkRequestBuilder<com.renium.sipkasku.work.RecurringWorker>(1, TimeUnit.DAYS)
            .build()

        val oneTime = androidx.work.OneTimeWorkRequestBuilder<com.renium.sipkasku.work.RecurringWorker>()
            .build()

        WorkManager.getInstance(applicationContext).enqueue(oneTime)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                Calendar.getInstance()
                recurringRepository.getAll().first()
            } catch(t: Throwable) {}
        }

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "recurring-worker",
            ExistingPeriodicWorkPolicy.KEEP,
            periodic
        )

        setContent {
            val themeMode by settingsRepository.getThemeMode().collectAsState(initial = "AUTO")
            val useDark = when (themeMode) {
                "LIGHT" -> false
                "DARK" -> true
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }

            com.renium.sipkasku.ui.theme.SipKasKuTheme(useDarkTheme = useDark) {
                Surface {
                    MainScreen(
                        transactionRepository,
                        pocketRepository,
                        categoryRepository,
                        recurringRepository,
                        settingsRepository
                    )
                }
            }
        }
    }
}
