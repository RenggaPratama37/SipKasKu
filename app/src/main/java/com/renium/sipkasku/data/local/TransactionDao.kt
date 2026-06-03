package com.renium.sipkasku.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

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
        DELETE FROM transactions
        WHERE category = :categoryName
    """)
    suspend fun deleteByCategory(
        categoryName: String
    )

    @Query("""
        SELECT * FROM transactions
        ORDER BY date DESC
    """)
    fun getAllTransactions(): Flow<List<TransactionEntity>>
}
