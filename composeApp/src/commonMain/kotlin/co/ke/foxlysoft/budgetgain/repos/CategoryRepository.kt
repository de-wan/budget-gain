package co.ke.foxlysoft.budgetgain.repos

import co.ke.foxlysoft.budgetgain.database.AppDatabase
import co.ke.foxlysoft.budgetgain.database.CategoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class CategoryRepository(
    db: AppDatabase
) {
    private val categoryDao = db.categoryDao()

    suspend fun upsertCategory(categoryEntity: CategoryEntity) = categoryDao.upsert(categoryEntity)

    suspend fun deleteCategory(categoryEntity: CategoryEntity) = categoryDao.delete(categoryEntity)

    fun getBudgetCategories(budgetId: Long): Flow<List<CategoryEntity>> {
        return categoryDao.getBudgetCategories(budgetId)
    }

    fun getCategory(categoryId: Long): Flow<CategoryEntity?> {
        return categoryDao.getCategory(categoryId)
    }
}