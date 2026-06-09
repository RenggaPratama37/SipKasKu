package com.renium.sipkasku.data.repository

import com.renium.sipkasku.data.local.TransactionDao
import com.renium.sipkasku.data.local.TransactionEntity

class TransactionRepository(
    private val dao: TransactionDao
) {

    fun getAllTransactions() =
        dao.getAllTransactions()

    suspend fun insertTransaction(
        transaction: TransactionEntity
    ) {
        dao.insertTransaction(transaction)
    }

    suspend fun deleteTransaction(
        transaction: TransactionEntity
    ) {
        dao.deleteTransaction(transaction)
    }

    suspend fun deleteByPocketId(
        pocketId: Int
    ) = dao.deleteByPocketId(pocketId)

    suspend fun deleteByCategory(
        categoryName: String
    ) {
        // find category id by name and delete by id
        // repository does not have category DAO; caller should pass id instead. Keep method for compatibility but no-op.
    }

    suspend fun countByRecurringAndDate(
        recurringId: Int,
        startOfDay: Long,
        endOfDay: Long
    ): Int = dao.countByRecurringAndDate(recurringId, startOfDay, endOfDay)

    suspend fun deleteByCategoryId(categoryId: Int) = dao.deleteByCategoryId(categoryId)
}
