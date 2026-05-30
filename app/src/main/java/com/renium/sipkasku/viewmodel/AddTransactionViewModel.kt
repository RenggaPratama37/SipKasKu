package com.renium.sipkasku.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.renium.sipkasku.data.local.TransactionEntity
import com.renium.sipkasku.data.repository.TransactionRepository

import kotlinx.coroutines.launch

class AddTransactionViewModel(
    private val repository: TransactionRepository,
    private val pocketRepository: com.renium.sipkasku.data.repository.PocketRepository? = null
) : ViewModel() {

    fun saveTransaction(
        title: String,
        amount: Double,
        category: String,
        isIncome: Boolean,
        date: Long,
        pocketId: Int? = null
    ) {

        viewModelScope.launch {

            repository.insertTransaction(
                TransactionEntity(
                    title = title,
                    amount = amount,
                    category = category,
                    isIncome = isIncome,
                    date = date,
                    pocketId = pocketId
                )
            )

            // adjust pocket balance if provided
            if (pocketId != null && pocketRepository != null) {
                // expense should decrease balance, income increases
                val delta = if (isIncome) amount else -amount
                try {
                    pocketRepository.adjustBalance(pocketId, delta)
                } catch (t: Throwable) {
                    // ignore balance adjustment failures for now
                }
            }
        }
    }
}
