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
        WHERE categoryId = :categoryId
    """)
    suspend fun deleteByCategoryId(
        categoryId: Int
    )

    @Query("""
        UPDATE transactions
        SET pocketId = :toPocketId
        WHERE pocketId = :fromPocketId
    """)
    suspend fun updatePocketId(fromPocketId: Int, toPocketId: Int)

    @Query("""
        UPDATE transactions
        SET categoryId = :toCategoryId
        WHERE categoryId = :fromCategoryId
    """)
    suspend fun updateCategoryId(fromCategoryId: Int, toCategoryId: Int)

    @Query("""
        SELECT * FROM transactions
        ORDER BY date DESC
    """)
    fun getAllTransactions(): Flow<List<TransactionEntity>>
}
