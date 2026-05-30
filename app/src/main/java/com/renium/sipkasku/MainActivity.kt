package com.renium.sipkasku

import android.os.Bundle

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.renium.sipkasku.data.local.AppDatabase
import com.renium.sipkasku.data.repository.CategoryRepository
import com.renium.sipkasku.data.repository.PocketRepository
import com.renium.sipkasku.data.repository.RecurringRepository
import com.renium.sipkasku.data.repository.SettingsRepository
import com.renium.sipkasku.data.repository.TransactionRepository
import com.renium.sipkasku.ui.layout.MainScreen
import androidx.lifecycle.lifecycleScope
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

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

        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "money_manager_db"
        ).addMigrations(MIGRATION_1_2, MIGRATION_2_3).build()

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
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val today = Calendar.getInstance()
                val day = today.get(Calendar.DAY_OF_MONTH)

                val recurrings = recurringRepository.getAll()
                // collect once
                recurrings.collect { list ->
                    list.forEach { r ->
                        val lastRunDay = if (r.lastRun == 0L) -1 else Calendar.getInstance().apply { timeInMillis = r.lastRun }.get(Calendar.DAY_OF_MONTH)
                        if (r.dayOfMonth == day && lastRunDay != day) {
                            // insert transaction
                            transactionRepository.insertTransaction(
                                com.renium.sipkasku.data.local.TransactionEntity(
                                    title = r.title,
                                    amount = r.amount,
                                    category = r.category,
                                    isIncome = r.isIncome,
                                    date = System.currentTimeMillis()
                                )
                            )
                            // update lastRun
                            recurringRepository.update(r.copy(lastRun = System.currentTimeMillis()))
                        }
                    }
                }
            } catch (t: Throwable) {
                // ignore recurring failures
            }
        }

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
