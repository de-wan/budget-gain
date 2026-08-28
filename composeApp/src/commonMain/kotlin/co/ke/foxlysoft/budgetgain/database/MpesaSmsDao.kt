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
    suspend fun existsByRef(ref: String): Boolean

    @Query("SELECT * FROM MpesaSmsEntity WHERE transactionId = 0 AND (subjectPrimaryIdentifier LIKE '%' || :search || '%' OR subjectSecondaryIdentifier LIKE '%' || :search || '%') AND isIgnored = false AND dateTime BETWEEN :from AND :to ORDER BY dateTime DESC LIMIT :limit OFFSET :offset")
    suspend fun getPagingUncategorizedMpesaSms(limit: Int, offset: Int, search: String? = null, from: Long, to: Long) : List<MpesaSmsEntity>

    @Query("SELECT * FROM MpesaSmsEntity WHERE subjectPrimaryIdentifier = :primaryIdentifier AND subjectPrimaryIdentifierType = :primaryIdentifierType AND subjectSecondaryIdentifier = :secondaryIdentifier AND subjectSecondaryIdentifierType = :secondaryIdentifierType AND transactionId = 0 AND dateTime BETWEEN :from AND :to")
    suspend fun getMpesaSmsByIdentifier(primaryIdentifier: String, primaryIdentifierType: String, secondaryIdentifier: String, secondaryIdentifierType: String, from: Long, to: Long): List<MpesaSmsEntity>

    @Query("SELECT * FROM MpesaSmsEntity WHERE id IN (:ids)")
    suspend fun getMpesaSmsById(ids: List<Long>): List<MpesaSmsEntity>

    @Query("SELECT * FROM MpesaSmsEntity WHERE id = :id AND transactionId = 0 LIMIT 1")
    suspend fun getUncategorizedMpesaSmsById(id: Long): MpesaSmsEntity?

    @Query("UPDATE MpesaSmsEntity SET transactionId = 0 WHERE transactionId = :transactionId")
    suspend fun restoreUncategorizedSms(transactionId: Long)

    @Query("UPDATE MpesaSmsEntity SET isIgnored = 1 WHERE id = :id")
    suspend fun ignoreMpesaSms(id: Long)
}
