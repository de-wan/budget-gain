package co.ke.foxlysoft.budgetgain.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

class UncategorizedMpesaSmsScreenViewModel(
    private val mpesaSmsRepository: MpesaSmsRepository,
    private val categoryRepository: CategoryRepository,
    private val budgetRepository: BudgetRepository,
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository
): ViewModel() {
    private val _currentBudgetQueryState =
        MutableStateFlow<QueryState>(QueryState.LOADING)
    val currentBudgetQueryState: StateFlow<QueryState>
        get() = _currentBudgetQueryState.asStateFlow()

    private val _currentBudget =
        MutableStateFlow<BudgetEntity>(BudgetEntity())
    val currentBudget: StateFlow<BudgetEntity>
        get() = _currentBudget.asStateFlow()

    private val budgetFromDate = MutableStateFlow<Long?>(null)
    private val budgetToDate = MutableStateFlow<Long?>(null)

    private val _selectableCategories = MutableStateFlow<List<CategoryEntity>>(emptyList())
    val selectableCategories: StateFlow<List<CategoryEntity>> = _selectableCategories

    private val _search = MutableStateFlow("")
    val search: StateFlow<String> = _search.asStateFlow()

    fun onSearchChange(search: String) {
        _search.value = search
    }

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
                            getBudgetDates(currentBudget)
                            _currentBudgetQueryState.value = QueryState.COMPLETE
                        }
                    }
                }

            }
        )
    }

    private suspend fun getBudgetDates(budgetEntity: BudgetEntity? = null): Pair<Long, Long> {
        if (budgetFromDate.value != null && budgetToDate.value != null) {
            return budgetFromDate.value!! to budgetToDate.value!!
        }

        val budget = budgetEntity ?: budgetRepository.getCurrentBudget()
        val splitYearMonth = budget.yearMonth.split("-").map{ it.toInt() }

        val yearMonth = YearMonth(splitYearMonth[0], Month(splitYearMonth[1]))
        budgetFromDate.value = yearMonth.firstDay.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        val nextMonth = yearMonth.plus(1, DateTimeUnit.MONTH)
        budgetToDate.value = nextMonth.firstDay.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds() - 1

        return budgetFromDate.value!! to budgetToDate.value!!
    }

    suspend fun getUncategorizedMpesaSms(limit: Int, offset: Int): List<MpesaSmsEntity> {
        val budgetDates = getBudgetDates()
        val from = budgetDates.first
        val to = budgetDates.second

        val search = _search.value
        return mpesaSmsRepository.getPagingUncategorizedMpesaSms(limit, offset, search, from, to)
    }

    private var _searchJob: Job? = null
    // Function to update the search query
    fun updateCategorySearchQuery(query: String) {
        _searchJob?.cancel()
        _searchJob = viewModelScope.launch {
            if (currentBudgetQueryState.value != QueryState.COMPLETE) {
                currentBudgetQueryState.first { state -> state == QueryState.COMPLETE }
            }
            if (query.isNotEmpty()) {
                delay(500)
                categoryRepository.searchBudgetCategoriesByName(currentBudget.value.id, query).collectLatest {
                    _selectableCategories.value = it
                }
            } else {
                _selectableCategories.value = categoryRepository
                    .getBudgetCategories(currentBudget.value.id)
                    .sortedBy { it.name.lowercase() }
            }
        }
    }

    private suspend fun categorizeSingleSms(categoryName: String, smsId: Long) {
        val smsToCategorize = mpesaSmsRepository.getUncategorizedMpesaSmsById(smsId) ?: return

        transactionRepository.getByRef(smsToCategorize.ref)?.let { existingTransaction ->
            smsToCategorize.transactionId = existingTransaction.id
            mpesaSmsRepository.updateMpesaSms(smsToCategorize)
            return
        }

        val budget = budgetRepository.getCurrentBudget()

        // check if sms timestamp is within budget time range
        // Convert millis to Instant, then to LocalDateTime in your timezone
        val instant = Instant.fromEpochMilliseconds(smsToCategorize.dateTime)
        val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val yearMonthStr = "${dateTime.year}-${dateTime.month.number.toString().padStart(2, '0')}"
        if (yearMonthStr != budget.yearMonth) {
            return
        }

        val merchantName = getMerchantNameFromSms(smsToCategorize)

        // get category id
        val category = categoryRepository.getBudgetCategoryByName(budget.id, categoryName)

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

    suspend fun ignoreSingleSms(smsToIgnore: MpesaSmsEntity) {
        val budget = budgetRepository.getCurrentBudget()

        // check if sms timestamp is within budget time range
        // Convert millis to Instant, then to LocalDateTime in your timezone
        val instant = Instant.fromEpochMilliseconds(smsToIgnore.dateTime)
        val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val yearMonthStr = "${dateTime.year}-${dateTime.month.number.toString().padStart(2, '0')}"
        if (yearMonthStr != budget.yearMonth) {
            return
        }

        mpesaSmsRepository.ignoreMpesaSms(smsToIgnore.id)
    }

    suspend fun categorizeSms(categoryName: String, smsToCategorize: MpesaSmsEntity, shouldCategorizeSimilarByMerchant: Boolean) {
        try {
            mpesaSmsRepository.withWriteTransaction {
                val smsIds = linkedSetOf(smsToCategorize.id)
                if (shouldCategorizeSimilarByMerchant) {
                    val (from, to) = getBudgetDates()
                    smsIds += mpesaSmsRepository.getMpesaSmsByIdentifier(
                        smsToCategorize.subjectPrimaryIdentifier,
                        smsToCategorize.subjectPrimaryIdentifierType,
                        smsToCategorize.subjectSecondaryIdentifier,
                        smsToCategorize.subjectSecondaryIdentifierType,
                        from,
                        to
                    ).map { it.id }
                }

                smsIds.forEach { categorizeSingleSms(categoryName, it) }
            }
        } catch (e: Exception) {
            Logger.e("Error categorizing mpesa sms", e)
            throw e
        }
    }

    suspend fun categorizeBulkSms(categoryName: String, selectedSmsIds: Set<Any>, shouldCategorizeSimilarByMerchant: Boolean) {
        try {
            mpesaSmsRepository.withWriteTransaction {
                val selectedIds = selectedSmsIds.mapNotNull { it as? Long }.toSet()
                val selectedSms = mpesaSmsRepository.getMpesaSmsById(selectedIds.toList())
                    .filter { it.transactionId == 0L }
                val similarSms = mutableListOf<MpesaSmsEntity>()

                if (shouldCategorizeSimilarByMerchant) {
                    val (from, to) = getBudgetDates()
                    selectedSms.forEach { sms ->
                        similarSms += mpesaSmsRepository.getMpesaSmsByIdentifier(
                            sms.subjectPrimaryIdentifier,
                            sms.subjectPrimaryIdentifierType,
                            sms.subjectSecondaryIdentifier,
                            sms.subjectSecondaryIdentifierType,
                            from,
                            to
                        )
                    }
                }

                val smsIds = categorizationSmsIds(selectedSms, similarSms)
                smsIds.forEach { categorizeSingleSms(categoryName, it) }
            }
        } catch (e: Exception) {
            Logger.e("Error bulk categorizing mpesa sms", e)
            throw e
        }
    }
}
