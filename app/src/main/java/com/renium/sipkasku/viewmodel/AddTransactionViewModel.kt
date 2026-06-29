package com.renium.sipkasku.viewmodel

import androidx.lifecycle.ViewModel
import com.renium.sipkasku.data.local.TransactionEntity
import com.renium.sipkasku.data.repository.PocketRepository
import com.renium.sipkasku.data.repository.TransactionRepository

class AddTransactionViewModel(
    private val repository: TransactionRepository,
    private val pocketRepository: PocketRepository? = null
) : ViewModel() {

    suspend fun trySaveTransaction(
        title: String,
        amount: Double,
        categoryId: Int?,
        isIncome: Boolean,
        date: Long,
        pocketId: Int? = null,
        note: String? = null
    ): Boolean {
        return try {
            // if this is an expense and pocket provided, ensure sufficient balance
            if (!isIncome && pocketId != null && pocketRepository != null) {
                val pocket = pocketRepository.getPocketById(pocketId)
                if (pocket == null || pocket.balance < amount) {
                    return false
                }
            }

            // perform insert and balance update
            repository.insertTransaction(
                TransactionEntity(
                    title = title,
                    amount = amount,
                    categoryId = categoryId,
                    isIncome = isIncome,
                    date = date,
                    pocketId = pocketId,
                    note = note
                )
            )

            if (pocketId != null && pocketRepository != null) {
                val delta = if(isIncome) amount else -amount
                pocketRepository.adjustBalance(pocketId, delta)
            }
            true
        } catch (t: Throwable) { false }
    }

    suspend fun tryUpdateTransaction(
        id: Int,
        title: String,
        amount: Double,
        categoryId: Int?,
        isIncome: Boolean,
        date: Long,
        pocketId: Int? = null,
        note: String? = null
    ): Boolean {
        return try{
            val existing = repository.getById(id) ?: return false

            existing.pocketId?.let { oldPocketId ->
                val revert = if (existing.isIncome) -existing.amount else existing.amount
                pocketRepository?.adjustBalance(oldPocketId, revert)
            }

            if (!isIncome && pocketId != null && pocketRepository != null) {
                val pocket = pocketRepository.getPocketById(pocketId)
                if (pocket == null || pocket.balance < amount ) return false
            }

            repository.updateTransaction(
                TransactionEntity(
                    id = id,
                    title = title,
                    amount = amount,
                    categoryId = categoryId,
                    isIncome = isIncome,
                    date = date,
                    pocketId = pocketId,
                    note = note
                )
            )

            pocketId?.let { newPocketId ->
                val delta = if (isIncome) amount else -amount
                pocketRepository?.adjustBalance(newPocketId, delta)
            }
            true
        } catch(t: Throwable) { false }
    }
}
