package com.renium.sipkasku.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PocketDao {

    @Query("SELECT * FROM pockets ORDER BY createdAt DESC")
    fun getAllPockets(): Flow<List<Pocket>>

    @Query("SELECT * FROM pockets WHERE id = :id LIMIT 1")
    suspend fun getPocketById(id: Int): Pocket?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPocket(pocket: Pocket): Long

    @Update
    suspend fun updatePocket(pocket: Pocket)

    @Delete
    suspend fun deletePocket(pocket: Pocket)
}
