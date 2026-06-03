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
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "money_manager_db"
        ).fallbackToDestructiveMigration()
            .build()

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
