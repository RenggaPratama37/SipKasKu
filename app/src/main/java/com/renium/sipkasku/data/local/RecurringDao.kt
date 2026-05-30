package com.renium.sipkasku.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringDao {

    @Query("SELECT * FROM recurrings ORDER BY dayOfMonth ASC")
    fun getAll(): Flow<List<Recurring>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recurring: Recurring): Long

    @Update
    suspend fun update(recurring: Recurring)

    @Delete
    suspend fun delete(recurring: Recurring)
}
