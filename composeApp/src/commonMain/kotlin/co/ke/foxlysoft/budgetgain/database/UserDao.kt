package co.ke.foxlysoft.budgetgain.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface UserDao {
    @Upsert
    suspend fun upsert(userEntity: UserEntity)

    @Query("SELECT * FROM UserEntity")
    suspend fun getAll(): List<UserEntity>

    @Query("SELECT * FROM UserEntity WHERE id = :id")
    suspend fun get(id: Int): UserEntity?
}