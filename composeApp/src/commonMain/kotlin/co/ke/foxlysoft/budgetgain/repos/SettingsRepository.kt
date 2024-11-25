package co.ke.foxlysoft.budgetgain.repos

import co.ke.foxlysoft.budgetgain.database.AppDatabase
import co.ke.foxlysoft.budgetgain.database.SettingsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

class SettingsRepository (
    db: AppDatabase
){
    private val settingsDao = db.settingsDao()

    fun getSetting(key: String): Flow<SettingsEntity> {
        return settingsDao.getSetting(key)
            .mapNotNull { it }
    }
    suspend fun setSetting(key: String, value: String){
        settingsDao.upsert(SettingsEntity(key, value))
    }
}