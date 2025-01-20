package co.ke.foxlysoft.budgetgain.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Upsert
    suspend fun upsert(categoryEntity: CategoryEntity)

    @Delete
    suspend fun delete(categoryEntity: CategoryEntity)

    @Query("SELECT * FROM CategoryEntity WHERE budgetId = :budgetId")
    fun getBudgetCategoriesFlow(budgetId: Long): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM CategoryEntity WHERE budgetId = :budgetId")
    suspend fun getBudgetCategories(budgetId: Long): List<CategoryEntity>

    @Query("SELECT * FROM CategoryEntity WHERE id = :categoryId")
    fun getCategoryFlow(categoryId: Long): Flow<CategoryEntity>

    @Query("SELECT * FROM CategoryEntity WHERE id = :categoryId")
    suspend fun getCategory(categoryId: Long): CategoryEntity
}