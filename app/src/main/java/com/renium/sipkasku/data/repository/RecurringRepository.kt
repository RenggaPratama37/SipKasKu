package com.renium.sipkasku.data.repository

import com.renium.sipkasku.data.local.Recurring
import com.renium.sipkasku.data.local.RecurringDao
import kotlinx.coroutines.flow.Flow

class RecurringRepository(
    private val dao: RecurringDao
) {

    fun getAll(): Flow<List<Recurring>> = dao.getAll()

    suspend fun insert(r: Recurring) = dao.insert(r)

    suspend fun update(r: Recurring) = dao.update(r)

    suspend fun delete(r: Recurring) = dao.delete(r)
}
