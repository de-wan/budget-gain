package co.ke.foxlysoft.budgetgain.repos

import co.ke.foxlysoft.budgetgain.database.AccountEntity
import co.ke.foxlysoft.budgetgain.database.AccountHolderType
import co.ke.foxlysoft.budgetgain.database.AppDatabase
import co.ke.foxlysoft.budgetgain.database.BudgetEntity
import kotlinx.coroutines.flow.Flow

class AccountRepository(db: AppDatabase) {
    private val accountDao = db.accountDao()

    suspend fun getOrCreateBudgetAccount(budget: BudgetEntity): AccountEntity {
        var budgetAccount = accountDao.getBudgetAccount(budget.id)
        if (budgetAccount == null || budgetAccount.id == 0L) {
            budgetAccount = AccountEntity(
                type = "debit",
                name = "Budget ${budget.name} Account",
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

    suspend fun getAccount(accountId: Long): AccountEntity {
        return accountDao.getAccount(accountId)
    }

   fun getMerchantAccounts(): Flow<List<AccountEntity>> {
        return accountDao.getMerchantAccounts()
    }

    fun getSelectableMerchantAccounts(search: String): Flow<List<AccountEntity>> {
        println("search: $search")
        return accountDao.getSelectableMerchantAccounts(AccountHolderType.MERCHANT, "%${search}%")
    }
}