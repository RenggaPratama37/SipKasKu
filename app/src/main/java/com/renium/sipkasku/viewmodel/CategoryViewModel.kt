package com.renium.sipkasku.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renium.sipkasku.data.local.Category
import com.renium.sipkasku.data.repository.TransactionRepository
import com.renium.sipkasku.data.repository.CategoryRepository
import com.renium.sipkasku.data.repository.RecurringRepository
import kotlinx.coroutines.launch

class CategoryViewModel (
    private val categoryRepository : CategoryRepository,
    private val transactionRepository : TransactionRepository,
    private val recurringRepository : RecurringRepository
) : ViewModel() {

    var pendingDeleteCategory by mutableStateOf<Category?>(null)
        private set

    var pendingDeleteTransactionCount by mutableIntStateOf(0)
        private set

    var pendingDeleteRecurringCount by mutableIntStateOf(0)
        private set

    fun requestDeleteCategory(category: Category) {
        pendingDeleteCategory = category
        viewModelScope.launch {
            val txCount = transactionRepository.countByCategoryId(category.id)
            val recCount = recurringRepository.countByCategoryId(category.id)
            pendingDeleteTransactionCount = txCount
            pendingDeleteRecurringCount = recCount
        }
    }

    fun confirmDeleteCategory() {
        val category = pendingDeleteCategory ?: return
        viewModelScope.launch {
            categoryRepository.delete(category)
            pendingDeleteCategory = null
            pendingDeleteTransactionCount = 0
            pendingDeleteRecurringCount = 0
        }
    }

    fun cancelDeleteCategory() {
        pendingDeleteCategory = null
        pendingDeleteTransactionCount = 0
        pendingDeleteRecurringCount = 0
    }
}
