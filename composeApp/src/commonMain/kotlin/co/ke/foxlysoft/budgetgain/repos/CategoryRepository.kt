package co.ke.foxlysoft.budgetgain.repos

import co.ke.foxlysoft.budgetgain.database.AppDatabase
import co.ke.foxlysoft.budgetgain.database.BudgetEntity
import co.ke.foxlysoft.budgetgain.database.CategoryEntity
import kotlinx.coroutines.flow.Flow

class CategoryRepository(
    db: AppDatabase
) {
    private val categoryDao = db.categoryDao()

    suspend fun upsertCategory(categoryEntity: CategoryEntity) = categoryDao.upsert(categoryEntity)

    suspend fun deleteCategory(categoryEntity: CategoryEntity) = categoryDao.delete(categoryEntity)

    fun getBudgetCategoriesFlow(budgetId: Long): Flow<List<CategoryEntity>> {
        return categoryDao.getBudgetCategoriesFlow(budgetId)
    }

    suspend fun getBudgetCategories(budgetId: Long): List<CategoryEntity> {
        return categoryDao.getBudgetCategories(budgetId)
    }

    fun getCategoryFlow(categoryId: Long): Flow<CategoryEntity> {
        return categoryDao.getCategoryFlow(categoryId)
    }

    suspend fun getCategory(categoryId: Long): CategoryEntity {
        return categoryDao.getCategory(categoryId)
    }

    fun searchBudgetCategoriesByName(budgetId: Long, search: String): Flow<List<CategoryEntity>> {
        println("search: $search")
        return categoryDao.searchBudgetCategoriesByName(budgetId, "%${search}%")
    }

    fun getCategoryByName(categoryName: String): CategoryEntity {
        return categoryDao.getCategoryByName(categoryName)
    }
}