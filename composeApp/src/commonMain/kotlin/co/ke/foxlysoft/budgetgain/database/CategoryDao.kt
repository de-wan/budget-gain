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

    @Query("""SELECT * FROM CategoryEntity WHERE budgetId = :budgetId 
        ORDER BY
            CASE
                WHEN amount = 0 THEN 0.0 
                ELSE (CAST(spentAmount AS FLOAT) / CAST(amount AS FLOAT)) 
            END ASC, 
            amount DESC, 
            name ASC
        LIMIT :limit OFFSET :offset""")
    suspend fun getPagingBudgetCategories(budgetId: Long, limit: Int, offset: Int) : List<CategoryEntity>

    @Query("SELECT * FROM CategoryEntity WHERE budgetId = :budgetId")
    suspend fun getBudgetCategories(budgetId: Long): List<CategoryEntity>

    @Query("SELECT * FROM CategoryEntity WHERE id = :categoryId")
    fun getCategoryFlow(categoryId: Long): Flow<CategoryEntity>

    @Query("SELECT * FROM CategoryEntity WHERE id = :categoryId")
    suspend fun getCategory(categoryId: Long): CategoryEntity

    @Query("SELECT * FROM CategoryEntity WHERE budgetId = :budgetId AND name LIKE :search ORDER BY name DESC LIMIT 10")
    fun searchBudgetCategoriesByName(budgetId: Long, search: String): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM CategoryEntity WHERE name = :name")
    fun getCategoryByName(name: String): CategoryEntity
}