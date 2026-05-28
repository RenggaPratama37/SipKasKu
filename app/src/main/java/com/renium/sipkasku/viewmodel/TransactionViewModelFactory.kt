package com.renium.sipkasku.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.renium.sipkasku.data.repository.TransactionRepository

class TransactionViewModelFactory(
    private val repository: TransactionRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        return when {

            modelClass.isAssignableFrom(
                HomeViewModel::class.java
            ) -> {
                HomeViewModel(repository) as T
            }

            modelClass.isAssignableFrom(
                AddTransactionViewModel::class.java
            ) -> {
                AddTransactionViewModel(repository) as T
            }

            modelClass.isAssignableFrom(
                StatisticsViewModel::class.java
            ) -> {
                StatisticsViewModel(repository) as T
            }

            else -> {
                throw IllegalArgumentException(
                    "Unknown ViewModel"
                )
            }
        }
    }
}
