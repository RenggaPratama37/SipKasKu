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
import com.renium.sipkasku.data.repository.PocketRepository
import com.renium.sipkasku.data.repository.TransactionRepository
import com.renium.sipkasku.ui.layout.MainScreen

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

        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "money_manager_db"
        ).addMigrations(MIGRATION_1_2).build()

        val transactionRepository = TransactionRepository(
            db.transactionDao()
        )

        val pocketRepository = PocketRepository(
            db.pocketDao()
        )

        setContent {
            com.renium.sipkasku.ui.theme.SipKasKuTheme {
                Surface {
                    MainScreen(transactionRepository, pocketRepository)
                }
            }
        }
    }
}
