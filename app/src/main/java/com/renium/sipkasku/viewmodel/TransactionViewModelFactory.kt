package com.renium.sipkasku.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.renium.sipkasku.data.repository.CategoryRepository
import com.renium.sipkasku.data.repository.TransactionRepository
import com.renium.sipkasku.data.repository.PocketRepository

class TransactionViewModelFactory(
    private val repository: TransactionRepository,
    private val pocketRepository: PocketRepository? = null,
    private val categoryRepository: CategoryRepository? = null
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        return when {

            modelClass.isAssignableFrom(
                HomeViewModel::class.java
            ) -> {
                HomeViewModel(repository,pocketRepository) as T
            }

            modelClass.isAssignableFrom(
                AddTransactionViewModel::class.java
            ) -> {
                AddTransactionViewModel(repository, pocketRepository) as T
            }

            modelClass.isAssignableFrom(StatisticsViewModel::class.java) -> {
                val catRepo = categoryRepository
                    ?: throw IllegalArgumentException("CategoryRepository required for StatisticsViewModel")
                val pocketRepo = pocketRepository
                    ?: throw IllegalArgumentException("PocketRepository required for StatisticsViewModel")
                StatisticsViewModel(repository, catRepo, pocketRepo) as T
            }

            else -> {
                throw IllegalArgumentException(
                    "Unknown ViewModel"
                )
            }
        }
    }
}
