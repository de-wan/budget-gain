package co.ke.foxlysoft.budgetgain.database

import androidx.room.Dao
import androidx.room.Upsert

@Dao
interface CategoryDao {
    @Upsert
    suspend fun upsert(categoryEntity: CategoryEntity)
}