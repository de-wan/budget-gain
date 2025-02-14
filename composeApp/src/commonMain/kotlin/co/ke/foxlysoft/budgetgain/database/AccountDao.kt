package co.ke.foxlysoft.budgetgain.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

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

    @Query("SELECT * FROM AccountEntity WHERE holderType = :holderType")
    fun getMerchantAccounts(holderType: AccountHolderType = AccountHolderType.MERCHANT): Flow<List<AccountEntity>>

    @Query("SELECT * FROM AccountEntity WHERE holderType = :holderType ORDER BY name DESC LIMIT :limit OFFSET :offset")
    suspend fun getPagingMerchantAccounts(limit: Int, offset: Int, holderType: AccountHolderType = AccountHolderType.MERCHANT): List<AccountEntity>

    @Query("SELECT * FROM AccountEntity WHERE holderType = :holderType AND name LIKE :search ORDER BY name DESC LIMIT 10")
    fun getSelectableMerchantAccounts(holderType: AccountHolderType = AccountHolderType.MERCHANT, search: String = "%%"): Flow<List<AccountEntity>>

    @Query("SELECT * FROM AccountEntity WHERE id = :accountId")
    suspend fun getAccount(accountId: Long): AccountEntity

    @Query("SELECT * FROM AccountEntity WHERE id = :accountId")
    fun getAccountFlow(accountId: Long): Flow<AccountEntity>

    @Query("SELECT * FROM TransactionEntity WHERE creditAccountId = :merchantAccountId AND budgetId = :budgetId")
    suspend fun getMerchantAccountTransactionsForBudget(merchantAccountId: Long, budgetId: Long): List<TransactionEntity>
}