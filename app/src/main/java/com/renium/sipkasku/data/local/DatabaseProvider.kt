package com.renium.sipkasku.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseProvider {

    @Volatile
    private var instance: AppDatabase? = null

    private val MIGRATION_1_2 = object: Migration(1,2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS 'pockets' ('id' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 'name' TEXT NOT NULL, 'balance' REAL NOT NULL, 'cratedAt' INTEGER NOT NULL"
            )
            try{
                database.execSQL(
                    "ALTER TABLE 'transactions' ADD COLUMN 'pocketId' INTEGER"
                )
            } catch (t: Throwable) {}
        }
    }

    private val MIGRATION_2_3 = object: Migration(2,3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS `categories` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `type` TEXT NOT NULL)"
            )
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS `recurrings` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `amount` REAL NOT NULL, `category` TEXT NOT NULL, `isIncome` INTEGER NOT NULL, `dayOfMonth` INTEGER NOT NULL, `lastRun` INTEGER NOT NULL)"
            )
        }
    }

    private val MIGRATION_3_4 = object: Migration(3,4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            try {
                database.execSQL("ALTER TABLE `recurrings` ADD COLUMN `categoryId` INTEGER")
                database.execSQL("ALTER TABLE `recurrings` ADD COLUMN `pocketId` INTEGER")
                database.execSQL("ALTER TABLE `recurrings` ADD COLUMN `frequency` TEXT DEFAULT 'MONTHLY'")
                database.execSQL("ALTER TABLE `recurrings` ADD COLUMN `dayOfWeek` INTEGER")
                database.execSQL("ALTER TABLE `recurrings` ADD COLUMN `isActive` INTEGER DEFAULT 1")
                database.execSQL("ALTER TABLE `recurrings` ADD COLUMN `createdAt` INTEGER DEFAULT 0")
            } catch (t: Throwable) {}
        }
    }

    private val MIGRATION_4_5 = object:  Migration(4,5) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "ALTER TABLE 'transactions' ADD COLUMN 'recurringId' INTEGER DEFAULT NULL"
            )
        }
    }

    fun get(context: Context) : AppDatabase {
        return instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "money_manager_db"
            )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                .build()
                .also { instance= it }
        }
    }

}