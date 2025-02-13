package co.ke.foxlysoft.budgetgain.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Transaction
import co.ke.foxlysoft.budgetgain.database.AccountEntity
import co.ke.foxlysoft.budgetgain.database.AccountType
import co.ke.foxlysoft.budgetgain.database.BudgetEntity
import co.ke.foxlysoft.budgetgain.database.CategoryEntity
import co.ke.foxlysoft.budgetgain.database.MpesaSmsEntity
import co.ke.foxlysoft.budgetgain.database.TransactionEntity
import co.ke.foxlysoft.budgetgain.repos.AccountRepository
import co.ke.foxlysoft.budgetgain.repos.BudgetRepository
import co.ke.foxlysoft.budgetgain.repos.CategoryRepository
import co.ke.foxlysoft.budgetgain.repos.MpesaSmsRepository
import co.ke.foxlysoft.budgetgain.repos.TransactionRepository
import co.ke.foxlysoft.budgetgain.utils.QueryState
import co.ke.foxlysoft.budgetgain.utils.PaginationState
import co.ke.foxlysoft.budgetgain.utils.centsToString
import co.ke.foxlysoft.budgetgain.utils.dateTimeMillisToString
import co.ke.foxlysoft.budgetgain.utils.getMerchantNameFromSms
import co.touchlab.kermit.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UncategorizedMpesaSmsScreenViewModel(
    private val mpesaSmsRepository: MpesaSmsRepository,
    private val categoryRepository: CategoryRepository,
    private val budgetRepository: BudgetRepository,
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository
): ViewModel() {
    companion object {
        const val PAGE_SIZE = 5
        const val INITIAL_PAGE = 0
    }

    private val _currentBudgetQueryState =
        MutableStateFlow<QueryState>(QueryState.LOADING)
    val currentBudgetQueryState: StateFlow<QueryState>
        get() = _currentBudgetQueryState.asStateFlow()

    private val _smsList =
        MutableStateFlow<List<MpesaSmsEntity>>(emptyList())
    val smsList: StateFlow<List<MpesaSmsEntity>>
        get() = _smsList.asStateFlow()

    private val _pagingState =
        MutableStateFlow<PaginationState>(PaginationState.LOADING)
    val pagingState: StateFlow<PaginationState>
        get() = _pagingState.asStateFlow()

    var page = CategoryDetailsScreenViewModel.INITIAL_PAGE
    var canPaginate by mutableStateOf(false)

    private val _currentBudget =
        MutableStateFlow<BudgetEntity>(BudgetEntity())
    val currentBudget: StateFlow<BudgetEntity>
        get() = _currentBudget.asStateFlow()

    private val _selectableCategories = MutableStateFlow<List<CategoryEntity>>(emptyList())
    val selectableCategories: StateFlow<List<CategoryEntity>> = _selectableCategories

    init {
        budgetRepository.getCurrentBudget(
            onStarted = {
                _currentBudgetQueryState.value = QueryState.LOADING
            },
            onComplete = { currentBudgetFlow ->
                viewModelScope.launch {
                    currentBudgetFlow.collect { currentBudget ->
                        if (currentBudget == null) {
                            _currentBudgetQueryState.value = QueryState.NO_RESULTS
                        } else {
                            _currentBudget.value = currentBudget
                            _currentBudgetQueryState.value = QueryState.COMPLETE
                        }
                    }
                }

            }
        )
    }

    fun clearPaging() {
        page = 0
        _pagingState.update { PaginationState.LOADING }
        canPaginate = false
    }

    fun getUncategorizedMpesaSms() {
        if (page == INITIAL_PAGE || (canPaginate) && _pagingState.value == PaginationState.REQUEST_INACTIVE) {
            _pagingState.update { if (page == INITIAL_PAGE) PaginationState.LOADING else PaginationState.PAGINATING }
        }

        viewModelScope.launch {
            try {
                val result = mpesaSmsRepository.getPagingUncategorizedMpesaSms(
                    PAGE_SIZE, page * PAGE_SIZE
                )
                canPaginate = result.size == PAGE_SIZE

                if (page == INITIAL_PAGE) {
                    if (result.isEmpty()) {
                        _pagingState.update { PaginationState.EMPTY }
                        return@launch
                    }
                    _smsList.value = result
                } else {
                    _smsList.value += result
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

    private var _searchJob: Job? = null
    // Function to update the search query
    fun updateCategorySearchQuery(query: String) {
        if (currentBudgetQueryState.value != QueryState.COMPLETE) {
            return
        }

        _searchJob?.cancel()
        _searchJob = viewModelScope.launch {
            if (query.isNotEmpty()) {
                delay(500)
                categoryRepository.searchBudgetCategoriesByName(currentBudget.value.id, query).collectLatest {
                    _selectableCategories.value = it
                }
            } else {
                _selectableCategories.value = emptyList()
            }
        }
    }

    private suspend fun categorizeSingleSms(categoryName: String, smsToCategorize: MpesaSmsEntity) {
        val merchantName = getMerchantNameFromSms(smsToCategorize)

        // get category id
        val category = categoryRepository.getCategoryByName(categoryName)

        // Get or create merchant account
        var merchantAccount = accountRepository.getByMerchantName(merchantName)
        if (merchantAccount == null) {
            merchantAccount = AccountEntity(
                type = "credit",
                name = "$merchantName Account",
                merchantName = merchantName,
                balance = 0L,
                merchantDefaultCategoryId = category.id,
            )

            accountRepository.upsertAccount(merchantAccount)
            merchantAccount = accountRepository.getByMerchantName(merchantName)
        }

        // Get budget
        val budget = budgetRepository.getCurrentBudget()

        // Get or create main account
        val budgetAccount = accountRepository.getOrCreateBudgetAccount(budget)

        var transactionTimestamp = dateTimeMillisToString(smsToCategorize.dateTime)
        // replace T with a space
        transactionTimestamp = transactionTimestamp.replace("T", " ")

        // prepare transaction
        val transaction = TransactionEntity(
            ref = smsToCategorize.ref,
            type = AccountType.CREDIT,
            description = "${smsToCategorize.smsType} subject: ${smsToCategorize.subjectPrimaryIdentifierType}.${smsToCategorize.subjectPrimaryIdentifier} ${smsToCategorize.subjectSecondaryIdentifierType}.${smsToCategorize.subjectSecondaryIdentifier} amount: ${centsToString(smsToCategorize.amount)}",
            budgetId = budget.id,
            debitAccountId = budgetAccount.id,
            creditAccountId = merchantAccount!!.id,
            categoryId = category.id,
            amount = smsToCategorize.amount,
            timestamp = transactionTimestamp,
        )

        val transactionId = transactionRepository.upsertTransaction(transaction)

        // update category spent amount
        category.spentAmount += smsToCategorize.amount
        categoryRepository.upsertCategory(category)

        // update budget
        budget.spentAmount += smsToCategorize.amount
        budgetRepository.upsertBudget(budget)

        // update account balances
        budgetAccount.balance -= smsToCategorize.amount
        accountRepository.upsertAccount(budgetAccount)

        merchantAccount.balance += smsToCategorize.amount
        accountRepository.upsertAccount(merchantAccount)

        smsToCategorize.transactionId = transactionId
        mpesaSmsRepository.updateMpesaSms(smsToCategorize)
    }

    @Transaction
    fun categorizeSms(categoryName: String, smsToCategorize: MpesaSmsEntity, shouldCategorizeSimilarByMerchant: Boolean, onComplete: () -> Unit, onError: (Throwable) -> Unit) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    categorizeSingleSms(categoryName, smsToCategorize)

                    if (shouldCategorizeSimilarByMerchant) {
                        // retrieve all sms with similar identifier
                        val primaryIdentifier = smsToCategorize.subjectPrimaryIdentifier
                        val primaryIdentifierType = smsToCategorize.subjectPrimaryIdentifierType
                        val secondaryIdentifier = smsToCategorize.subjectSecondaryIdentifier
                        val secondaryIdentifierType = smsToCategorize.subjectSecondaryIdentifierType

                        val smsList = mpesaSmsRepository.getMpesaSmsByIdentifier(
                            primaryIdentifier,
                            primaryIdentifierType,
                            secondaryIdentifier,
                            secondaryIdentifierType
                        )
                        smsList.forEach {
                            categorizeSingleSms(categoryName, it)
                        }
                    }

                    onComplete()
                }
            } catch (e: Exception) {
                Logger.e("Error categorizing mpesa sms", e)
                onError(e)
                return@launch
            }
        }


    }
}