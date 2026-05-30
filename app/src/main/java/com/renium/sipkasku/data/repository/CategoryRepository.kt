package com.renium.sipkasku.data.repository

import com.renium.sipkasku.data.local.Category
import com.renium.sipkasku.data.local.CategoryDao
import kotlinx.coroutines.flow.Flow

class CategoryRepository(
    private val dao: CategoryDao
) {

    fun getAll(): Flow<List<Category>> = dao.getAll()

    fun getByType(type: String): Flow<List<Category>> = dao.getByType(type)

    suspend fun insert(category: Category) = dao.insert(category)

    suspend fun delete(category: Category) = dao.delete(category)
}
