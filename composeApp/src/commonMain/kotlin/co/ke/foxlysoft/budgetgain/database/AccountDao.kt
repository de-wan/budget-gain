package co.ke.foxlysoft.budgetgain.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface AccountDao {
    @Upsert
    suspend fun upsert(account: AccountEntity):Long

    @Delete
    suspend fun delete(account: AccountEntity)

    @Query("SELECT * FROM AccountEntity WHERE holderType = :holderType AND merchantName = :merchantName")
    suspend fun getByMerchantName(merchantName: String, holderType: AccountHolderType = AccountHolderType.MERCHANT): AccountEntity?

    @Query("SELECT * FROM AccountEntity WHERE type = 'debit' AND merchantName = 'Main'")
    suspend fun getMainAccount(): AccountEntity?

    @Query("SELECT * FROM AccountEntity WHERE holderType = :holderType AND budgetId = :budgetId")
    suspend fun getBudgetAccount(budgetId: Long, holderType: AccountHolderType = AccountHolderType.BUDGET): AccountEntity?

    @Query("SELECT * FROM AccountEntity WHERE id = :accountId")
    fun getAccount(accountId: Long): AccountEntity
}