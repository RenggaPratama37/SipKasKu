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
        pocketId: Int? = null
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
                    pocketId = pocketId
                )
            )

            if (pocketId != null && pocketRepository != null) {
                val delta = if (isIncome) amount else -amount
                try {
                    pocketRepository.adjustBalance(pocketId, delta)
                } catch (_: Throwable) {
                }
            }
            true
        } catch (t: Throwable) {
            false
        }
    }
}
