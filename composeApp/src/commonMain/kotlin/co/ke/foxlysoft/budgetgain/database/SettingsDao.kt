package co.ke.foxlysoft.budgetgain.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Upsert
    suspend fun upsert(settingsEntity: SettingsEntity)

    @Delete
    suspend fun delete(settingsEntity: SettingsEntity)

    @Query("SELECT * FROM SettingsEntity")
    fun getAll(): Flow<List<SettingsEntity>>

    @Query("SELECT * FROM SettingsEntity WHERE settingKey = :key")
    fun getSetting(key: String): Flow<SettingsEntity?>

    // set key
    @Query("UPDATE SettingsEntity SET value = :value WHERE settingKey = :key")
    suspend fun setSetting(key: String, value: String)

    // has key
    @Query("SELECT EXISTS(SELECT 1 FROM SettingsEntity WHERE settingKey = :key)")
    suspend fun hasKey(key: String): Boolean
}