package com.renium.sipkasku.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renium.sipkasku.data.local.Category
import com.renium.sipkasku.data.repository.TransactionRepository
import com.renium.sipkasku.data.repository.CategoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CategoryViewModel (
    private val categoryRepository : CategoryRepository,
    private val transactionRepository : TransactionRepository
) : ViewModel() {
    val incomeCategories = categoryRepository.getByType("INCOME")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000),emptyList())

    val expenseCategories = categoryRepository.getByType("EXPENSE")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addCategory(name: String, type: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            categoryRepository.insert(Category(name = name.trim(), type = type))
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            transactionRepository.deleteByCategoryId(category.id)
            categoryRepository.delete(category)
        }
    }




}
