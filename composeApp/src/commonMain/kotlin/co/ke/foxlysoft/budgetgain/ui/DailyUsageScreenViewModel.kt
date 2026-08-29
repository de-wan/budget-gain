package co.ke.foxlysoft.budgetgain.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ke.foxlysoft.budgetgain.database.BudgetEntity
import co.ke.foxlysoft.budgetgain.database.TransactionEntity
import co.ke.foxlysoft.budgetgain.database.MpesaSmsEntity
import co.ke.foxlysoft.budgetgain.my_calendar.DayStatus
import co.ke.foxlysoft.budgetgain.repos.AccountRepository
import co.ke.foxlysoft.budgetgain.repos.BudgetRepository
import co.ke.foxlysoft.budgetgain.repos.SettingsRepository
import co.ke.foxlysoft.budgetgain.repos.MpesaSmsRepository
import co.ke.foxlysoft.budgetgain.repos.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlinx.datetime.yearMonth
import kotlin.time.Clock

class DailyUsageScreenViewModel (
    private val budgetRepository: BudgetRepository,
    private val transactionsRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val settingsRepository: SettingsRepository,
    private val mpesaSmsRepository: MpesaSmsRepository
): ViewModel() {
    private companion object {
        const val DEFAULT_DAILY_USAGE_IN_CENTS = 30_000L
        const val DAILY_USAGE_SETTING_KEY = "dailyUsageInCents"
        const val UNKNOWN_MERCHANT = "Unknown merchant"
    }

    private val _isCalendarLoading = MutableStateFlow(true)
    val isCalendarLoading: StateFlow<Boolean>
        get() = _isCalendarLoading.asStateFlow()

    private val _dailyUsageInCents = MutableStateFlow(DEFAULT_DAILY_USAGE_IN_CENTS)
    val dailyUsageInCents: StateFlow<Long>
        get() = _dailyUsageInCents.asStateFlow()

    private val _optimumDailyUsageInCents = MutableStateFlow(0L)
    val optimumDailyUsageInCents: StateFlow<Long>
        get() = _optimumDailyUsageInCents.asStateFlow()

    private val _uncategorizedTransactions = MutableStateFlow<List<MpesaSmsEntity>>(emptyList())
    val uncategorizedTransactions: StateFlow<List<MpesaSmsEntity>>
        get() = _uncategorizedTransactions.asStateFlow()

    private val _isUncategorizedLoading = MutableStateFlow(false)
    val isUncategorizedLoading: StateFlow<Boolean>
        get() = _isUncategorizedLoading.asStateFlow()

    private var uncategorizedLoadedDate: LocalDate? = null

    private val _currentBudget =
        MutableStateFlow(BudgetEntity())
    val currentBudget: StateFlow<BudgetEntity>
        get() = _currentBudget.asStateFlow()

    private val _month = MutableStateFlow(0)
    val month: StateFlow<Int>
        get() = _month.asStateFlow()

    private val _year = MutableStateFlow(0)
    val year: StateFlow<Int>
        get() = _year.asStateFlow()

    private val _selectedDayStatus = MutableStateFlow(DayStatus())
    val selectedDayStatus: StateFlow<DayStatus>
        get() = _selectedDayStatus.asStateFlow()

    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    val selectedDate: StateFlow<LocalDate?>
        get() = _selectedDate.asStateFlow()

    private val _selectedDayTransactions = MutableStateFlow<List<DailyTransactionUiModel>>(emptyList())
    val selectedDayTransactions: StateFlow<List<DailyTransactionUiModel>>
        get() = _selectedDayTransactions.asStateFlow()

    private var transactionItemsByDate: Map<LocalDate, List<DailyTransactionUiModel>> = emptyMap()
    private val merchantNameCache = mutableMapOf<Long, String>()

    private val _dayStatus = MutableStateFlow<Map<LocalDate, DayStatus>>(emptyMap())
    val dayStatus: StateFlow<Map<LocalDate, DayStatus>>
        get() = _dayStatus.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.getSetting(DAILY_USAGE_SETTING_KEY).collect { setting ->
                val storedDailyUsage = setting.value.toLongOrNull()
                    ?.takeIf { it > 0 }
                    ?: DEFAULT_DAILY_USAGE_IN_CENTS
                if (storedDailyUsage != _dailyUsageInCents.value) {
                    _dailyUsageInCents.value = storedDailyUsage
                    _currentBudget.value
                        .takeIf { it.yearMonth.isNotBlank() }
                        ?.let { computeDayStatuses(it) }
                }
            }
        }

        budgetRepository.getCurrentBudget(
            onStarted = {},
            onComplete = { currentBudgetFlow ->
                viewModelScope.launch {
                    currentBudgetFlow.collect { currentBudget ->
                        if (currentBudget != null) {
                            _currentBudget.value = currentBudget
                            val splitMonthYear = currentBudget.yearMonth.split("-").map { it.toInt() }
                            _month.value = splitMonthYear[1]
                            _year.value = splitMonthYear[0]
                            computeDayStatuses(currentBudget)
                        } else {
                            _isCalendarLoading.value = false
                        }
                    }
                }
            }
        )
    }

    suspend fun computeDayStatuses(budget: BudgetEntity) {
        _isCalendarLoading.value = true
        try {
            val (year, month) = budget.yearMonth.split("-").map(String::toInt)
            val firstDayOfMonth = LocalDate(year, month, 1)
            val daysInMonth = firstDayOfMonth.yearMonth.numberOfDays
            val dailyUsageLimit = _dailyUsageInCents.value
            val monthPrefix = "${budget.yearMonth}-"
            val usedAmountByDay = LongArray(daysInMonth + 1)
            val transactionsByDate = mutableMapOf<LocalDate, MutableList<TransactionEntity>>()
            val merchantAccountIds = mutableSetOf<Long>()

            for (transaction in transactionsRepository.getBudgetTransactions(budget.id)) {
                if (transaction.timestamp.length < 10 ||
                    !transaction.timestamp.startsWith(monthPrefix)
                ) {
                    continue
                }

                val dayOfMonth = transaction.timestamp.substring(8, 10).toIntOrNull()
                if (dayOfMonth == null || dayOfMonth !in 1..daysInMonth) continue

                val date = LocalDate(year, month, dayOfMonth)
                transactionsByDate.getOrPut(date, ::mutableListOf).add(transaction)
                usedAmountByDay[dayOfMonth] += transaction.amount
                merchantAccountIds += transaction.creditAccountId
            }

            for (accountId in merchantAccountIds) {
                if (accountId in merchantNameCache) continue

                merchantNameCache[accountId] =
                    runCatching { accountRepository.getAccount(accountId) }
                        .getOrNull()
                        ?.let { account -> account.merchantName.ifBlank { account.name } }
                        ?.ifBlank { UNKNOWN_MERCHANT }
                        ?: UNKNOWN_MERCHANT
            }

            transactionItemsByDate = transactionsByDate.mapValues { (_, dailyTransactions) ->
                dailyTransactions.map { transaction ->
                    DailyTransactionUiModel(
                        transaction = transaction,
                        merchantName = merchantNameCache[transaction.creditAccountId]
                            ?: UNKNOWN_MERCHANT
                    )
                }
            }

            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val elapsedDays = if (today.year == year && today.month.number == month) {
                today.day
            } else {
                daysInMonth
            }
            val spentToDate = (1..elapsedDays).sumOf { day -> usedAmountByDay[day] }
            _optimumDailyUsageInCents.value = if (spentToDate == 0L) {
                0L
            } else {
                (spentToDate + elapsedDays - 1) / elapsedDays
            }

            var carriedForwardAmount = 0L
            val statusByDate = buildMap {
                for (dayOfMonth in 1..daysInMonth) {
                    val date = LocalDate(year, month, dayOfMonth)
                    val usedAmount = usedAmountByDay[dayOfMonth]
                    val broughtForwardAmount = carriedForwardAmount
                    val availableAmount = broughtForwardAmount + dailyUsageLimit
                    val isUsed = usedAmount > 0
                    val isOverUsed = usedAmount > dailyUsageLimit

                    carriedForwardAmount = availableAmount - usedAmount

                    put(
                        date,
                        DayStatus(
                            broughtForwardAmount = broughtForwardAmount,
                            isMovedForward = carriedForwardAmount > 0,
                            carriedForwardAmount = carriedForwardAmount,
                            isUsed = isUsed,
                            usedAmount = usedAmount,
                            isOverUsed = isOverUsed,
                            overUsedAmount = (usedAmount - dailyUsageLimit).coerceAtLeast(0)
                        )
                    )
                }
            }

            _dayStatus.value = statusByDate
            _selectedDate.value?.let(::selectDay)
        } finally {
            _isCalendarLoading.value = false
        }
    }

    fun selectDay(date: LocalDate) {
        if (_selectedDate.value != date) {
            uncategorizedLoadedDate = null
            _uncategorizedTransactions.value = emptyList()
        }
        _selectedDate.value = date
        _selectedDayStatus.value = _dayStatus.value[date] ?: DayStatus()
        _selectedDayTransactions.value = transactionItemsByDate[date].orEmpty()
    }

    fun updateDailyUsage(amountInCents: Long) {
        if (amountInCents <= 0) return
        viewModelScope.launch {
            settingsRepository.setSetting(DAILY_USAGE_SETTING_KEY, amountInCents.toString())
        }
    }

    fun loadUncategorizedTransactions() {
        val date = _selectedDate.value ?: return
        if (uncategorizedLoadedDate == date || _isUncategorizedLoading.value) return

        viewModelScope.launch {
            _isUncategorizedLoading.value = true
            try {
                val timeZone = TimeZone.currentSystemDefault()
                val from = date.atStartOfDayIn(timeZone).toEpochMilliseconds()
                val to = date.plus(1, DateTimeUnit.DAY)
                    .atStartOfDayIn(timeZone)
                    .toEpochMilliseconds() - 1
                _uncategorizedTransactions.value =
                    mpesaSmsRepository.getPagingUncategorizedMpesaSms(
                        limit = 200,
                        offset = 0,
                        search = "",
                        from = from,
                        to = to
                    )
                uncategorizedLoadedDate = date
            } finally {
                _isUncategorizedLoading.value = false
            }
        }
    }

    fun ignoreUncategorizedTransaction(sms: MpesaSmsEntity) {
        viewModelScope.launch {
            mpesaSmsRepository.ignoreMpesaSms(sms.id)
            _uncategorizedTransactions.value =
                _uncategorizedTransactions.value.filterNot { it.id == sms.id }
        }
    }

    fun refreshSelectedDay() {
        uncategorizedLoadedDate = null
        loadUncategorizedTransactions()
        _currentBudget.value
            .takeIf { it.yearMonth.isNotBlank() }
            ?.let { budget ->
                viewModelScope.launch { computeDayStatuses(budget) }
            }
    }
}

data class DailyTransactionUiModel(
    val transaction: TransactionEntity,
    val merchantName: String
)
