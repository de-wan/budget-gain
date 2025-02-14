package co.ke.foxlysoft.budgetgain.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ke.foxlysoft.budgetgain.database.AccountEntity
import co.ke.foxlysoft.budgetgain.database.TransactionEntity
import co.ke.foxlysoft.budgetgain.repos.AccountRepository
import co.ke.foxlysoft.budgetgain.repos.BudgetRepository
import co.ke.foxlysoft.budgetgain.repos.TransactionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class MerchantTransactionsScreenViewModel(
    private val merchantId: Long,
    private val accountRepository: AccountRepository,
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository,
): ViewModel() {
    val currentMerchant: StateFlow<AccountEntity> = accountRepository.getMerchantFlow(merchantId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AccountEntity()
        )

    suspend fun getMerchantTransactions(limit : Int, offset : Int): List<TransactionEntity> {
        val currentBudget = budgetRepository.getCurrentBudget()
        return transactionRepository.getPagingMerchantTransactions(currentBudget.id, merchantId, limit, offset)
    }
}