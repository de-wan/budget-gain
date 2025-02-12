package co.ke.foxlysoft.budgetgain.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert

@Dao
interface MpesaSmsDao {
    @Upsert
    suspend fun upsert(mpesaSmsEntity: MpesaSmsEntity)

    @Update
    suspend fun update(mpesaSmsEntity: MpesaSmsEntity)

    @Query("SELECT EXISTS(SELECT 1 FROM MpesaSmsEntity WHERE ref = :ref)")
    fun existsByRef(ref: String): Boolean

    @Query("SELECT * FROM MpesaSmsEntity WHERE transactionId = 0 ORDER BY dateTime DESC LIMIT :limit OFFSET :offset")
    suspend fun getPagingUncategorizedMpesaSms(limit: Int, offset: Int) : List<MpesaSmsEntity>

    @Query("SELECT * FROM MpesaSmsEntity WHERE subjectPrimaryIdentifier = :primaryIdentifier AND subjectPrimaryIdentifierType = :primaryIdentifierType AND subjectSecondaryIdentifier = :secondaryIdentifier AND subjectSecondaryIdentifierType = :secondaryIdentifierType AND transactionId = 0")
    suspend fun getMpesaSmsByIdentifier(primaryIdentifier: String, primaryIdentifierType: String, secondaryIdentifier: String, secondaryIdentifierType: String): List<MpesaSmsEntity>
}