package co.ke.foxlysoft.budgetgain.repos

import co.ke.foxlysoft.budgetgain.database.AppDatabase
import co.ke.foxlysoft.budgetgain.database.BudgetEntity
import co.ke.foxlysoft.budgetgain.database.CategoryEntity
import co.ke.foxlysoft.budgetgain.database.CommonCategoryDefinition
import co.ke.foxlysoft.budgetgain.database.SubCategoryEntity
import androidx.room.immediateTransaction
import androidx.room.useWriterConnection
import kotlinx.coroutines.flow.Flow

class CategoryRepository(
    private val db: AppDatabase
) {
    private val categoryDao = db.categoryDao()
    private val subCategoryDao = db.subCategoryDao()

    suspend fun upsertCategory(categoryEntity: CategoryEntity) = categoryDao.upsert(categoryEntity)

    suspend fun deleteCategory(categoryEntity: CategoryEntity) = categoryDao.delete(categoryEntity)

    fun getBudgetCategoriesFlow(budgetId: Long): Flow<List<CategoryEntity>> {
        return categoryDao.getBudgetCategoriesFlow(budgetId)
    }

    suspend fun getPagingBudgetCategories(budgetId: Long, limit: Int, offset: Int) = categoryDao.getPagingBudgetCategories(budgetId, limit, offset)

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

    suspend fun getBudgetCategoryByName(budgetId: Long, categoryName: String): CategoryEntity {
        return categoryDao.getBudgetCategoryByName(budgetId, categoryName)
    }

    suspend fun getBudgetCategoryByCatalogKey(budgetId: Long, catalogKey: String) =
        categoryDao.getBudgetCategoryByCatalogKey(budgetId, catalogKey)

    fun getSubCategoriesFlow(categoryId: Long) = subCategoryDao.getForCategoryFlow(categoryId)

    suspend fun getSubCategories(categoryId: Long) = subCategoryDao.getForCategory(categoryId)

    suspend fun getSubCategory(id: Long) = subCategoryDao.get(id)

    suspend fun upsertSubCategory(subCategory: SubCategoryEntity) = subCategoryDao.upsert(subCategory)

    suspend fun deleteSubCategory(subCategory: SubCategoryEntity) = subCategoryDao.delete(subCategory)

    suspend fun installCommonCategory(
        budgetId: Long,
        definition: CommonCategoryDefinition,
        amount: Long = 0,
    ): Long = db.useWriterConnection { transactor ->
        transactor.immediateTransaction {
            categoryDao.getBudgetCategoryByCatalogKey(budgetId, definition.key)?.id ?: run {
                val categoryId = categoryDao.upsert(
                    CategoryEntity(
                        budgetId = budgetId,
                        catalogKey = definition.key,
                        name = definition.name,
                        amount = amount,
                        spentAmount = 0,
                        lightColorArgb = definition.lightColorArgb,
                        darkColorArgb = definition.darkColorArgb,
                        iconKey = definition.iconKey,
                    )
                )
                subCategoryDao.upsertAll(definition.subCategories.map { child ->
                    SubCategoryEntity(
                        categoryId = categoryId,
                        catalogKey = "${definition.key}.${child.key}",
                        name = child.name,
                        iconKey = child.iconKey,
                        lightColorArgb = child.lightColorArgb,
                        darkColorArgb = child.darkColorArgb,
                    )
                })
                categoryId
            }
        }
    }
}
