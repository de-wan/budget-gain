package co.ke.foxlysoft.budgetgain.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Upsert
    suspend fun upsert(budgetEntity: BudgetEntity):Long

    @Delete
    suspend fun delete(budgetEntity: BudgetEntity)

    @Query("SELECT * FROM BudgetEntity")
    fun getAll(): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM BudgetEntity WHERE isActive = 1")
    fun getCurrentBudget(): Flow<BudgetEntity?>

    @Query("SELECT * FROM BudgetEntity WHERE id = :budgetId")
    suspend fun getBudget(budgetId: Long): BudgetEntity

    // count number of budgets
    @Query("SELECT COUNT(1) FROM BudgetEntity")
    suspend fun countBudgets(): Int

    @Query("UPDATE BudgetEntity SET isActive = 0 WHERE isActive = 1")
    suspend fun deactivateBudget()

    @Query("UPDATE BudgetEntity SET isActive = 1 WHERE id = :budgetId")
    suspend fun activateBudgetPart(budgetId: Long)

    suspend fun activateBudget(budgetId: Long) {
        deactivateBudget()
        activateBudgetPart(budgetId)
    }

    @Query("SELECT * FROM BudgetEntity WHERE name LIKE :search ORDER BY name DESC LIMIT 10")
    fun searchBudgetsByName(search: String): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM BudgetEntity WHERE name = :name")
    suspend fun getBudgetByName(name: String): BudgetEntity?
}