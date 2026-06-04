package com.renium.sipkasku.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import com.renium.sipkasku.data.local.TransactionEntity
import com.renium.sipkasku.data.repository.TransactionRepository
import com.renium.sipkasku.data.repository.PocketRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: TransactionRepository,
    private val pocketRepository: PocketRepository? = null
) : ViewModel() {

    enum class FilterType { ALL, INCOME, EXPENSE }
    enum class SortType { DATE_DESC, DATE_ASC, AMOUNT_DESC, AMOUNT_ASC }

    private val _filter = MutableStateFlow(FilterType.ALL)
    private val _sort = MutableStateFlow(SortType.DATE_DESC)

    val transactions: StateFlow<List<TransactionEntity>> =
        repository
            .getAllTransactions()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    val visibleTransactions: StateFlow<List<TransactionEntity>> =
        combine(transactions, _filter, _sort) { list, filter, sort ->
            var filtered = when (filter) {
                FilterType.ALL -> list
                FilterType.INCOME -> list.filter { it.isIncome }
                FilterType.EXPENSE -> list.filter { !it.isIncome }
            }

            filtered = when (sort) {
                SortType.DATE_DESC -> filtered.sortedByDescending { it.date }
                SortType.DATE_ASC -> filtered.sortedBy { it.date }
                SortType.AMOUNT_DESC -> filtered.sortedByDescending { it.amount }
                SortType.AMOUNT_ASC -> filtered.sortedBy { it.amount }
            }

            filtered
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    fun deleteTransaction(
        transaction: TransactionEntity
    ) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
            transaction.pocketId?.let { 
                pocketId -> 
                    val delta = 
                        if(transaction.isIncome)
                            -transaction.amount
                        else
                            transaction.amount
                    pocketRepository?.adjustBalance(
                        pocketId,
                        delta
                    )
            }
        }
    }

    fun setFilter(filter: FilterType) {
        _filter.value = filter
    }

    fun setSort(sort: SortType) {
        _sort.value = sort
    }

    // Expose current filter and sort to UI
    val currentFilter: StateFlow<FilterType>
        get() = _filter

    val currentSort: StateFlow<SortType>
        get() = _sort

    fun restoreTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.insertTransaction(transaction)
            transaction.pocketId?.let {
                pocketId ->
                    val delta = 
                        if(transaction.isIncome)
                            transaction.amount
                        else
                            -transaction.amount
                    pocketRepository?.adjustBalance(
                        pocketId,
                        delta
                    )
            }
        }
    }
}
