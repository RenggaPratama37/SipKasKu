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
    suspend fun deleteByCategoryId(categoryId: Int) = dao.deleteByCategoryId(categoryId)

    suspend fun updatePocketId(fromPocketId: Int, toPocketId: Int) = dao.updatePocketId(fromPocketId, toPocketId)

    suspend fun updateCategoryId(fromCategoryId: Int, toCategoryId: Int) = dao.updateCategoryId(fromCategoryId, toCategoryId)
}
