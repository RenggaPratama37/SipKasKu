package com.renium.sipkasku.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert
    suspend fun insertTransaction(
        transaction: TransactionEntity
    )

    @Delete
    suspend fun deleteTransaction(
        transaction: TransactionEntity
    )

    @Query("""
        DELETE FROM transactions
        WHERE pocketId = :pocketId
    """)
    suspend fun deleteByPocketId(
        pocketId: Int
    )

    @Query("""
        SELECT * FROM transactions
        ORDER BY date DESC
    """)
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("""
        SELECT COUNT(*) FROM transactions
        WHERE recurringId = :recurringId
        AND date >= :startOfDay
        AND date <= :endOfDay 
    """)
    suspend fun countByRecurringAndDate(
        recurringId: Int,
        startOfDay: Long,
        endOfDay: Long
    ): Int

    @Query("""
        SELECT COUNT(*) FROM transactions WHERE categoryId = :categoryId
    """)
    suspend fun countByCategoryId(categoryId: Int): Int

    @Query("""
        SELECT * FROM transactions WHERE id = :id LIMIT 1
    """)
    suspend fun getById(id:Int): TransactionEntity?

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)
}
