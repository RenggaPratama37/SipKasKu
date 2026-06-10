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
import com.renium.sipkasku.data.local.DatabaseProvider
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

        val  db = DatabaseProvider.get(applicationContext)

        val transactionRepository = TransactionRepository(
            db.transactionDao()
        )

        val pocketRepository = PocketRepository(
            db.pocketDao()
        )

        val categoryRepository = CategoryRepository(db.categoryDao())
        val recurringRepository = RecurringRepository(db.recurringDao())
        val settingsRepository = SettingsRepository(applicationContext)

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
