package com.renium.sipkasku.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.renium.sipkasku.data.local.TransactionEntity
import com.renium.sipkasku.data.repository.TransactionRepository

import kotlinx.coroutines.launch

class AddTransactionViewModel(
    private val repository: TransactionRepository
) : ViewModel() {

    fun saveTransaction(
        title: String,
        amount: Double,
        category: String,
        isIncome: Boolean,
        date: Long
    ) {

        viewModelScope.launch {

            repository.insertTransaction(
                TransactionEntity(
                    title = title,
                    amount = amount,
                    category = category,
                    isIncome = isIncome,
                    date = date
                )
            )
        }
    }
}
