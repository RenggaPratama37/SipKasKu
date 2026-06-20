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

    suspend fun countByCategoryId(categoryId: Int): Int = dao.countByCategoryId(categoryId)

    suspend fun countByRecurringAndDate(
        recurringId: Int,
        startOfDay: Long,
        endOfDay: Long
    ): Int = dao.countByRecurringAndDate(recurringId, startOfDay, endOfDay)

}
