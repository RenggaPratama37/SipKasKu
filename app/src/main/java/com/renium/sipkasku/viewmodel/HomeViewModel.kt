package com.renium.sipkasku.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renium.sipkasku.data.local.TransactionEntity
import com.renium.sipkasku.data.repository.TransactionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
    repository: TransactionRepository
) : ViewModel() {

    val transactions: StateFlow<List<TransactionEntity>> =
        repository
            .getAllTransactions()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
}
