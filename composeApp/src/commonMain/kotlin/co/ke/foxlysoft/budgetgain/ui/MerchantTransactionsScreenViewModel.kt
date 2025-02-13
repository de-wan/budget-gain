package co.ke.foxlysoft.budgetgain.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ke.foxlysoft.budgetgain.database.AccountEntity
import co.ke.foxlysoft.budgetgain.database.TransactionEntity
import co.ke.foxlysoft.budgetgain.repos.AccountRepository
import co.ke.foxlysoft.budgetgain.repos.BudgetRepository
import co.ke.foxlysoft.budgetgain.repos.TransactionRepository
import co.ke.foxlysoft.budgetgain.utils.PaginationState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MerchantTransactionsScreenViewModel(
    private val merchantId: Long,
    private val accountRepository: AccountRepository,
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository,
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

    private var page = CategoryDetailsScreenViewModel.INITIAL_PAGE
    var canPaginate by mutableStateOf(false)

    val currentMerchant: StateFlow<AccountEntity> = accountRepository.getMerchantFlow(merchantId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AccountEntity()
        )

    fun getMerchantTransactions(merchantId: Long) {
        if (page == INITIAL_PAGE || (canPaginate) && _pagingState.value == PaginationState.REQUEST_INACTIVE) {
            _pagingState.update { if (page == INITIAL_PAGE) PaginationState.LOADING else PaginationState.PAGINATING }
        }

        viewModelScope.launch {
            try {
                val budget = budgetRepository.getCurrentBudget()

                val result = transactionRepository.getPagingMerchantTransactions(budget.id, merchantId,
                    PAGE_SIZE, page * PAGE_SIZE
                )
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
}