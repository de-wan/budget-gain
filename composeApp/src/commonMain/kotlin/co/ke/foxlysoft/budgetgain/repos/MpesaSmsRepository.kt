package co.ke.foxlysoft.budgetgain.repos

import co.ke.foxlysoft.budgetgain.database.AppDatabase
import co.ke.foxlysoft.budgetgain.database.MpesaSmsEntity

class MpesaSmsRepository(db: AppDatabase) {
    private val mpesaSmsDao = db.mpesaSmsDao()

    suspend fun upsertMpesaSms(mpesaSmsEntity: MpesaSmsEntity) {
        // only add if ref doesn't exist
        if (mpesaSmsDao.existsByRef(mpesaSmsEntity.ref)) {
            return
        }

        mpesaSmsDao.upsert(mpesaSmsEntity)
    }

    suspend fun updateMpesaSms(mpesaSmsEntity: MpesaSmsEntity) {
        mpesaSmsDao.update(mpesaSmsEntity)
    }

    suspend fun getPagingUncategorizedMpesaSms(limit: Int, offset: Int) = mpesaSmsDao.getPagingUncategorizedMpesaSms(limit, offset)

    suspend fun getMpesaSmsByIdentifier(primaryIdentifier: String, primaryIdentifierType: String, secondaryIdentifier: String?, secondaryIdentifierType: String?): List<MpesaSmsEntity> {
        return mpesaSmsDao.getMpesaSmsByIdentifier(primaryIdentifier, primaryIdentifierType, secondaryIdentifier ?: "", secondaryIdentifierType ?: "")
    }
}