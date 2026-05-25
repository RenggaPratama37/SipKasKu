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
}
