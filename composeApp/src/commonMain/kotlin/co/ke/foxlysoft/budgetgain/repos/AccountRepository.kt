package co.ke.foxlysoft.budgetgain.repos

import co.ke.foxlysoft.budgetgain.database.AccountEntity
import co.ke.foxlysoft.budgetgain.database.AccountHolderType
import co.ke.foxlysoft.budgetgain.database.AppDatabase
import co.ke.foxlysoft.budgetgain.database.BudgetEntity
import co.ke.foxlysoft.budgetgain.database.TransactionEntity
import kotlinx.coroutines.flow.Flow

class AccountRepository(db: AppDatabase) {
    private val accountDao = db.accountDao()

    suspend fun getOrCreateBudgetAccount(budget: BudgetEntity): AccountEntity {
        var budgetAccount = accountDao.getBudgetAccount(budget.id)
        if (budgetAccount == null || budgetAccount.id == 0L) {
            budgetAccount = AccountEntity(
                type = "debit",
                name = "Budget ${budget.yearMonth} Account",
                balance = budget.initialBalance,
                budgetId = budget.id,
                holderType = AccountHolderType.BUDGET,
            )
            accountDao.upsert(budgetAccount)
        }
        return budgetAccount
    }

    suspend fun upsertAccount(accountEntity: AccountEntity):Long = accountDao.upsert(accountEntity)

    suspend fun getByMerchantName(merchantName: String): AccountEntity? {
        return accountDao.getByMerchantName(merchantName)
    }

    fun getMerchantFlow(merchantId: Long): Flow<AccountEntity> {
        return accountDao.getAccountFlow(merchantId)
    }

    suspend fun getAccount(accountId: Long): AccountEntity {
        return accountDao.getAccount(accountId)
    }

    fun getMerchantAccounts(): Flow<List<AccountEntity>> {
        return accountDao.getMerchantAccounts()
    }

    suspend fun getPagingMerchantAccounts(limit: Int, offset: Int): List<AccountEntity> {
        return accountDao.getPagingMerchantAccounts(limit, offset)
    }

    fun getSelectableMerchantAccounts(search: String): Flow<List<AccountEntity>> {
        println("search: $search")
        return accountDao.getSelectableMerchantAccounts(AccountHolderType.MERCHANT, "%${search}%")
    }

    suspend fun getMerchantAccountTransactionsForBudget(merchantAccountId: Long, budgetId: Long): List<TransactionEntity> {
        return accountDao.getMerchantAccountTransactionsForBudget(merchantAccountId, budgetId)
    }
}