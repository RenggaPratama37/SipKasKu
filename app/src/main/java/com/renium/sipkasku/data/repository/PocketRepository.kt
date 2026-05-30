package com.renium.sipkasku.data.repository

import com.renium.sipkasku.data.local.Pocket
import com.renium.sipkasku.data.local.PocketDao
import kotlinx.coroutines.flow.Flow

class PocketRepository(
    private val pocketDao: PocketDao
) {

    fun getAllPockets(): Flow<List<Pocket>> = pocketDao.getAllPockets()

    suspend fun getPocketById(id: Int): Pocket? = pocketDao.getPocketById(id)

    suspend fun insertPocket(pocket: Pocket) = pocketDao.insertPocket(pocket)

    suspend fun updatePocket(pocket: Pocket) = pocketDao.updatePocket(pocket)

    suspend fun deletePocket(pocket: Pocket) = pocketDao.deletePocket(pocket)

    suspend fun adjustBalance(pocketId: Int, delta: Double) {
        val pocket = pocketDao.getPocketById(pocketId) ?: return
        val updated = pocket.copy(balance = pocket.balance + delta)
        pocketDao.updatePocket(updated)
    }
}
