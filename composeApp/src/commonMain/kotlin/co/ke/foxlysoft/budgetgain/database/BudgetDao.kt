package co.ke.foxlysoft.budgetgain.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
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
}