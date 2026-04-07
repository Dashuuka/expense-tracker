package com.expensetracker.data.repository

import com.expensetracker.data.db.CategoryDao
import com.expensetracker.data.db.CategoryEntity
import com.expensetracker.data.model.Category
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao
) {
    fun getAllCategories(): Flow<List<Category>> =
        categoryDao.getAllCategories().map { list -> list.map { it.toDomain() } }

    suspend fun addCategory(category: Category): Long =
        categoryDao.insert(category.toEntity())

    suspend fun updateCategory(category: Category) =
        categoryDao.update(category.toEntity())

    suspend fun deleteCategory(category: Category) =
        categoryDao.delete(category.toEntity())
}

fun CategoryEntity.toDomain() = Category(
    id = id, name = name, icon = icon, color = color, isDefault = isDefault
)

fun Category.toEntity() = CategoryEntity(
    id = id, name = name, icon = icon, color = color, isDefault = isDefault
)
