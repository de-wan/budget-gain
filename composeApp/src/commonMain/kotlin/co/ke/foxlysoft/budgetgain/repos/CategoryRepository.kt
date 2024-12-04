package co.ke.foxlysoft.budgetgain.repos

import co.ke.foxlysoft.budgetgain.database.AppDatabase
import co.ke.foxlysoft.budgetgain.database.CategoryEntity
import kotlinx.coroutines.flow.Flow

class CategoryRepository(
    db: AppDatabase
) {
    private val categoryDao = db.categoryDao()

    suspend fun upsertCategory(categoryEntity: CategoryEntity) = categoryDao.upsert(categoryEntity)

    fun getBudgetCategories(budgetId: Long): Flow<List<CategoryEntity>> {
        return categoryDao.getBudgetCategories(budgetId)
    }
}