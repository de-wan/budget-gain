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

    fun getMerchantAccount(transaction: TransactionEntity, onComplete: (AccountEntity) -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val acc = accountRepository.getAccount(transaction.creditAccountId)
                onComplete(acc)
            }
        }
    }

    @Transaction
    fun deleteTransaction(transaction: TransactionEntity, refreshAllPages: () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                // update credit account balance
                val creditAccount = accountRepository.getAccount(transaction.creditAccountId)
                creditAccount.balance -= transaction.amount
                accountRepository.upsertAccount(creditAccount)

                // update debit account balance
                val debitAccount = accountRepository.getAccount(transaction.debitAccountId)
                debitAccount.balance += transaction.amount
                accountRepository.upsertAccount(debitAccount)

                // update category spent amount
                val category = categoryRepository.getCategory(transaction.categoryId)
                category.spentAmount -= transaction.amount
                categoryRepository.upsertCategory(category)

                // update budget spent amount
                val budget = budgetRepository.getBudget(category.budgetId)
                budget.spentAmount -= transaction.amount
                budgetRepository.upsertBudget(budget)

                // delete transaction
                transactionRepository.deleteTransaction(transaction)

                refreshAllPages()
            }
        }
    }
}