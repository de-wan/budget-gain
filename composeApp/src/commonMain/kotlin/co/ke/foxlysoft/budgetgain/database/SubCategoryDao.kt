package co.ke.foxlysoft.budgetgain.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface SubCategoryDao {
    @Upsert
    suspend fun upsert(subCategory: SubCategoryEntity): Long

    @Upsert
    suspend fun upsertAll(subCategories: List<SubCategoryEntity>)

    @Delete
    suspend fun delete(subCategory: SubCategoryEntity)

    @Query("SELECT * FROM SubCategoryEntity WHERE categoryId = :categoryId ORDER BY name")
    fun getForCategoryFlow(categoryId: Long): Flow<List<SubCategoryEntity>>

    @Query("SELECT * FROM SubCategoryEntity WHERE categoryId = :categoryId ORDER BY name")
    suspend fun getForCategory(categoryId: Long): List<SubCategoryEntity>

    @Query("SELECT * FROM SubCategoryEntity WHERE id = :id")
    suspend fun get(id: Long): SubCategoryEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM SubCategoryEntity WHERE id = :id AND categoryId = :categoryId)")
    suspend fun belongsToCategory(id: Long, categoryId: Long): Boolean
}
