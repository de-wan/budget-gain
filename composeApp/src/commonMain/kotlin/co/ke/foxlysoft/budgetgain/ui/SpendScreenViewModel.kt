package co.ke.foxlysoft.budgetgain.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Transaction
import co.ke.foxlysoft.budgetgain.database.AccountEntity
import co.ke.foxlysoft.budgetgain.database.AccountType
import co.ke.foxlysoft.budgetgain.database.CategoryEntity
import co.ke.foxlysoft.budgetgain.database.TransactionEntity
import co.ke.foxlysoft.budgetgain.repos.AccountRepository
import co.ke.foxlysoft.budgetgain.repos.BudgetRepository
import co.ke.foxlysoft.budgetgain.repos.CategoryRepository
import co.ke.foxlysoft.budgetgain.repos.TransactionRepository
import co.touchlab.kermit.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
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
    private val categorySelection = CategorySelectionState(categoryId)

    val currentCategory: StateFlow<CategoryEntity?> = categorySelection.categoryId
        .flatMapLatest { selectedId ->
            if (selectedId == null) {
                flowOf(null)
            } else {
                categoryRepository.getCategoryFlow(selectedId)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _selectableCategories = MutableStateFlow<List<CategoryEntity>>(emptyList())
    val selectableCategories: StateFlow<List<CategoryEntity>> = _selectableCategories

    private val _merchantAccounts = MutableStateFlow<List<AccountEntity>>(emptyList())
    val merchantAccounts: StateFlow<List<AccountEntity>> = _merchantAccounts

    private var merchantSearchJob: Job? = null
    private var categorySearchJob: Job? = null

    // Function to update the search query
    fun updateMerchantSearchQuery(query: String) {
        merchantSearchJob?.cancel()
        merchantSearchJob = viewModelScope.launch {
            delay(500)
            accountRepository.getSelectableMerchantAccounts(query).collectLatest {
                _merchantAccounts.value = it
            }
        }
    }

    fun selectCategory(category: CategoryEntity) {
        categorySelection.select(category.id)
    }

    fun clearSelectedCategory() {
        categorySelection.clear()
    }

    fun updateCategorySearchQuery(query: String) {
        categorySearchJob?.cancel()
        categorySearchJob = viewModelScope.launch {
            val budgetId = currentCategory.value?.budgetId
                ?: categoryRepository.getCategory(categoryId).budgetId

            if (query.isNotBlank()) {
                delay(500)
                categoryRepository.searchBudgetCategoriesByName(budgetId, query).collectLatest {
                    _selectableCategories.value = it.sortedBy { category -> category.name.lowercase() }
                }
            } else {
                _selectableCategories.value = categoryRepository
                    .getBudgetCategories(budgetId)
                    .sortedBy { it.name.lowercase() }
            }
        }
    }

    @Transaction
    fun spend(onComplete:() -> Unit , onError: (Throwable) -> Unit, ref: String, merchantName: String, description: String, amount: Long, timestamp: String) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    // confirm no transaction with passed ref
                    if (transactionRepository.existsByRef(ref)) {
                        onError(Exception("Transaction with similar ref already exists"))
                        return@withContext
                    }

                    val currentCategoryProxy = currentCategory.value
                    if (currentCategoryProxy == null) {
                        onError(Exception("No category selected"))
                        return@withContext
                    }
                    // Get or create merchant account
                    var merchantAccount = accountRepository.getByMerchantName(merchantName)
                    if (merchantAccount == null) {
                        merchantAccount = AccountEntity(
                            type = "credit",
                            name = "$merchantName Account",
                            merchantName = merchantName,
                            balance = 0L,
                            merchantDefaultCategoryId = currentCategoryProxy.id,
                        )

                        accountRepository.upsertAccount(merchantAccount)
                        merchantAccount = accountRepository.getByMerchantName(merchantName)
                    }

                    // Get budget
                    val budget = budgetRepository.getBudget(currentCategoryProxy.budgetId)

                    // Get or create main account
                    val budgetAccount = accountRepository.getOrCreateBudgetAccount(budget)

                    // prepare transaction
                    val transaction = TransactionEntity(
                        ref = ref,
                        type = AccountType.CREDIT,
                        description = description,
                        budgetId = budget.id,
                        debitAccountId = budgetAccount.id,
                        creditAccountId = merchantAccount!!.id,
                        categoryId = currentCategoryProxy.id,
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

                onComplete()
            } catch (e: Exception) {
                Logger.e("Error spending", e)
                onError(e)
                return@launch
            }
        }
    }
}
