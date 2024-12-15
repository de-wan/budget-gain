package co.ke.foxlysoft.budgetgain.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Transaction
import co.ke.foxlysoft.budgetgain.database.AccountEntity
import co.ke.foxlysoft.budgetgain.database.AccountType
import co.ke.foxlysoft.budgetgain.database.BudgetEntity
import co.ke.foxlysoft.budgetgain.database.CategoryEntity
import co.ke.foxlysoft.budgetgain.database.TransactionEntity
import co.ke.foxlysoft.budgetgain.repos.AccountRepository
import co.ke.foxlysoft.budgetgain.repos.BudgetRepository
import co.ke.foxlysoft.budgetgain.repos.CategoryRepository
import co.ke.foxlysoft.budgetgain.repos.TransactionRepository
import co.touchlab.kermit.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SpendScreenViewModel(
    private val categoryId: Long,
    private val categoryRepository: CategoryRepository,
    private val accountRepository: AccountRepository,
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository
): ViewModel() {
    val currentCategory: StateFlow<CategoryEntity?> = categoryRepository.getCategory(categoryId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    @Transaction
    fun spend(onComplete:() -> Unit , onError: (Throwable) -> Unit, ref: String, merchantName: String, description: String, amount: Long, timestamp: String) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val currentCategoryProxy = currentCategory.value
                    if (currentCategoryProxy == null) {
                        onError(Exception("No category selected"))
                        return@withContext
                    }
                    // Get or create merchant account
                    var accountId: Long
                    var merchantAccount = accountRepository.getByMerchantName(merchantName)
                    if (merchantAccount == null) {
                        merchantAccount = AccountEntity(
                            type = "credit",
                            name = "$merchantName Account",
                            merchantName = merchantName,
                            balance = 0L,
                        )

                        accountId = accountRepository.upsertAccount(merchantAccount)

                    } else {
                        accountId = merchantAccount.id
                    }

                    // Get budget
                    val budget = budgetRepository.getBudget(currentCategoryProxy.budgetId)

                    // Get or create main account
                    var budgetAccount = accountRepository.getOrCreateBudgetAccount(budget)

                    // prepare transaction
                    val transaction = TransactionEntity(
                        ref = ref,
                        type = AccountType.CREDIT,
                        description = description,
                        debitAccountId = budgetAccount.id,
                        creditAccountId = accountId,
                        categoryId = categoryId,
                        amount = amount,
                        timestamp = timestamp,
                    )

                    transactionRepository.upsertTransaction(transaction)

                    // update category spent amount
                    currentCategoryProxy.spentAmount += amount
                    categoryRepository.upsertCategory(currentCategoryProxy)

                    // update budget
                    budget.spentAmount += amount
                    budgetRepository.upsertBudget(budget)

                    // update account balances
                    budgetAccount.balance -= amount
                    accountRepository.upsertAccount(budgetAccount)

                    merchantAccount.balance += amount
                    accountRepository.upsertAccount(merchantAccount)
                }
            } catch (e: Exception) {
                Logger.e("Error spending", e)
                onError(e)
                return@launch
            }
        }.invokeOnCompletion { onComplete() }
    }
}