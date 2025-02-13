package co.ke.foxlysoft.budgetgain.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ke.foxlysoft.budgetgain.database.AccountEntity
import co.ke.foxlysoft.budgetgain.database.SettingsEntity
import co.ke.foxlysoft.budgetgain.repos.AccountRepository
import co.ke.foxlysoft.budgetgain.repos.BudgetRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class MerchantsScreenViewModel(
    private val accountRepository: AccountRepository,
    private val budgetRepository: BudgetRepository,
): ViewModel() {
    val merchantAccounts: StateFlow<List<AccountEntity>> = accountRepository.getMerchantAccounts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

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