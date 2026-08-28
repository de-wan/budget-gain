package co.ke.foxlysoft.budgetgain.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Transaction
import co.ke.foxlysoft.budgetgain.database.AccountEntity
import co.ke.foxlysoft.budgetgain.database.CategoryEntity
import co.ke.foxlysoft.budgetgain.database.TransactionEntity
import co.ke.foxlysoft.budgetgain.repos.AccountRepository
import co.ke.foxlysoft.budgetgain.repos.BudgetRepository
import co.ke.foxlysoft.budgetgain.repos.CategoryRepository
import co.ke.foxlysoft.budgetgain.repos.MerchantSummary
import co.ke.foxlysoft.budgetgain.repos.MpesaSmsRepository
import co.ke.foxlysoft.budgetgain.repos.TransactionRepository
import co.ke.foxlysoft.budgetgain.utils.PaginationState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CategoryDetailsScreenViewModel(
    private val categoryId: Long,
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository,
    private val mpesaSmsRepository: MpesaSmsRepository,
    private val accountRepository: AccountRepository,
    private val budgetRepository: BudgetRepository,
): ViewModel() {
    val currentCategory: StateFlow<CategoryEntity> = categoryRepository.getCategoryFlow(categoryId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CategoryEntity(
                id = 0L,
                budgetId = 0L,
                name = "",
                amount = 0L,
                spentAmount = 0L,
                createdAt = 0L,
            )
        )

    suspend fun getCategoryTransactions(limit: Int, offset: Int): List<TransactionEntity> {
        return transactionRepository.getPagingCategoryTransactions(categoryId, limit, offset)
    }

    suspend fun getMerchantAccount(transaction: TransactionEntity): AccountEntity {
        return accountRepository.getAccount(transaction.creditAccountId)
    }

    @Transaction
    suspend fun deleteTransaction(transaction: TransactionEntity) {
        // update credit account balance
        val creditAccount = accountRepository.getAccount(transaction.creditAccountId)
        val debitAccount = accountRepository.getAccount(transaction.debitAccountId)
        val category = categoryRepository.getCategory(transaction.categoryId)
        val budget = budgetRepository.getBudget(category.budgetId)

        restoreTransactionAmounts(transaction, creditAccount, debitAccount, category, budget)

        accountRepository.upsertAccount(creditAccount)
        accountRepository.upsertAccount(debitAccount)
        categoryRepository.upsertCategory(category)
        budgetRepository.upsertBudget(budget)

        // restore linked SMS back to uncategorized state
        mpesaSmsRepository.restoreUncategorizedSms(transaction.id)

        // delete transaction
        transactionRepository.deleteTransaction(transaction)
    }

    suspend fun getMerchantSummaryForCategory(): List<MerchantSummary> {
        return transactionRepository.getMerchantSummaryForCategory(categoryId)
    }

    suspend fun getCurrentMonthDailySpendByCategory() : List<Pair<Int, Long>> {
        return transactionRepository.getCurrentMonthDailySpendByCategory(categoryId)
    }
}
