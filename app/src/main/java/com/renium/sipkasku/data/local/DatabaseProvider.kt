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
                "CREATE TABLE IF NOT EXISTS 'pockets' ('id') INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 'name' TEXT NOT NULL, 'balance' REAL NOT NULL, 'cratedAt' INTEGER NOT NULL"
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

    private val MIGRATION_5_6 = object: Migration(5, 6) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Transactions
            database.execSQL("""
            CREATE TABLE IF NOT EXISTS `transactions_new` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `title` TEXT NOT NULL,
                `amount` REAL NOT NULL,
                `categoryId` INTEGER,
                `isIncome` INTEGER NOT NULL,
                `date` INTEGER NOT NULL,
                `pocketId` INTEGER,
                `recurringId` INTEGER,
                FOREIGN KEY(`categoryId`) REFERENCES `categories`(`id`) ON DELETE CASCADE,
                FOREIGN KEY(`pocketId`) REFERENCES `pockets`(`id`) ON DELETE CASCADE
            )
        """)
            database.execSQL("""
            INSERT INTO `transactions_new` (id, title, amount, categoryId, isIncome, date, pocketId, recurringId)
            SELECT id, title, amount, categoryId, isIncome, date, pocketId, recurringId
            FROM `transactions`
        """)
            database.execSQL("DROP TABLE `transactions`")
            database.execSQL("ALTER TABLE `transactions_new` RENAME TO `transactions`")

            database.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_categoryId` ON `transactions`(`categoryId`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_pocketId` ON `transactions`(`pocketId`)")

            // Recurrings
            database.execSQL("""
            CREATE TABLE IF NOT EXISTS `recurrings_new` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `title` TEXT NOT NULL,
                `amount` REAL NOT NULL,
                `categoryId` INTEGER,
                `pocketId` INTEGER,
                `isIncome` INTEGER NOT NULL,
                `frequency` TEXT NOT NULL DEFAULT 'MONTHLY',
                `dayOfMonth` INTEGER NOT NULL,
                `dayOfWeek` INTEGER,
                `isActive` INTEGER NOT NULL DEFAULT 1,
                `lastRun` INTEGER NOT NULL DEFAULT 0,
                `lastFailedRun` INTEGER NOT NULL DEFAULT 0,
                `createdAt` INTEGER NOT NULL DEFAULT 0,
                FOREIGN KEY(`categoryId`) REFERENCES `categories`(`id`) ON DELETE CASCADE,
                FOREIGN KEY(`pocketId`) REFERENCES `pockets`(`id`) ON DELETE CASCADE
            )
        """)
            database.execSQL("""
            INSERT INTO `recurrings_new` (id, title, amount, categoryId, pocketId, isIncome, frequency, dayOfMonth, dayOfWeek, isActive, lastRun, lastFailedRun, createdAt)
            SELECT id, title, amount, categoryId, pocketId, isIncome, frequency, dayOfMonth, dayOfWeek, isActive, lastRun, 0, createdAt
            FROM `recurrings`
        """)
            database.execSQL("DROP TABLE `recurrings`")
            database.execSQL("ALTER TABLE `recurrings_new` RENAME TO `recurrings`")

            database.execSQL("CREATE INDEX IF NOT EXISTS `index_recurrings_categoryId` ON `recurrings`(`categoryId`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_recurrings_pocketId` ON `recurrings`(`pocketId`)")
        }
    }

    private val MIGRATION_6_7 = object : Migration(6,7) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "ALTER TABLE `transactions` ADD COLUMN `note` TEXT DEFAULT NULL"
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
                .addMigrations(
                    MIGRATION_1_2,
                    MIGRATION_2_3,
                    MIGRATION_3_4,
                    MIGRATION_4_5,
                    MIGRATION_5_6,
                    MIGRATION_6_7
                )
                .build()
                .also { instance= it }
        }
    }

}