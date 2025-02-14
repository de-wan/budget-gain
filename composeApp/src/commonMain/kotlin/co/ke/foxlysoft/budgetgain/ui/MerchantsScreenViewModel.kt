package co.ke.foxlysoft.budgetgain.ui

import androidx.lifecycle.ViewModel
import co.ke.foxlysoft.budgetgain.database.AccountEntity
import co.ke.foxlysoft.budgetgain.repos.AccountRepository
import co.ke.foxlysoft.budgetgain.repos.BudgetRepository

class MerchantsScreenViewModel(
    private val accountRepository: AccountRepository,
    private val budgetRepository: BudgetRepository,
): ViewModel() {

    suspend fun getMerchantAccounts(limit: Int, offset: Int): List<AccountEntity> {
        return accountRepository.getPagingMerchantAccounts(limit, offset)
    }

    suspend fun getMerchantAccountBudgetSpend(merchantAccountId: Long): Long {
        val budget = budgetRepository.getCurrentBudget()
        val transactions = accountRepository.getMerchantAccountTransactionsForBudget(merchantAccountId, budget.id)

        var totalSpent = 0L
        transactions.forEach { transaction ->
            totalSpent += transaction.amount
        }

        return totalSpent
    }
}