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
    companion object {
        const val PAGE_SIZE = 5
        const val INITIAL_PAGE = 0
    }

    private val _transactionsList =
        MutableStateFlow<List<TransactionEntity>>(emptyList())
    val transactionsList: StateFlow<List<TransactionEntity>>
        get() = _transactionsList.asStateFlow()

    private val _pagingState =
        MutableStateFlow<PaginationState>(PaginationState.LOADING)
    val pagingState: StateFlow<PaginationState>
        get() = _pagingState.asStateFlow()

    private var page = INITIAL_PAGE
    var canPaginate by mutableStateOf(false)

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

    fun getCategoryTransactions(categoryId: Long) {
        if (page == INITIAL_PAGE || (canPaginate) && _pagingState.value == PaginationState.REQUEST_INACTIVE) {
            _pagingState.update { if (page == INITIAL_PAGE) PaginationState.LOADING else PaginationState.PAGINATING }
        }

        viewModelScope.launch {
            try {
                val result = transactionRepository.getPagingCategoryTransactions(categoryId, PAGE_SIZE, page * PAGE_SIZE)
                canPaginate = result.size == PAGE_SIZE

                if (page == INITIAL_PAGE) {
                    if (result.isEmpty()) {
                        _pagingState.update { PaginationState.EMPTY }
                        return@launch
                    }
                    _transactionsList.value = result
                } else {
                    _transactionsList.value += result
                }

                _pagingState.update { PaginationState.REQUEST_INACTIVE }

                if (canPaginate) {
                    page++
                }

                if (!canPaginate) {
                    _pagingState.update { PaginationState.PAGINATION_EXHAUST }
                }
            } catch (e: Exception) {
                _pagingState.update { if (page == INITIAL_PAGE) PaginationState.ERROR else PaginationState.PAGINATION_EXHAUST }
            }
        }
    }

    fun clearPaging() {
        page = 0
        _pagingState.update { PaginationState.LOADING }
        canPaginate = false
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
    fun deleteTransaction(transaction: TransactionEntity) {
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

                _transactionsList.value = _transactionsList.value.filter { it.id != transaction.id }
            }
        }
    }
}