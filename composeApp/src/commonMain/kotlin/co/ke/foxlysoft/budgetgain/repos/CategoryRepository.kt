package co.ke.foxlysoft.budgetgain.repos

import co.ke.foxlysoft.budgetgain.database.AppDatabase
import co.ke.foxlysoft.budgetgain.database.CategoryEntity

class CategoryRepository(
    db: AppDatabase
) {
    private val categoryDao = db.categoryDao()

    suspend fun upsertCategory(categoryEntity: CategoryEntity) = categoryDao.upsert(categoryEntity)
}