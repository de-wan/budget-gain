package co.ke.foxlysoft.budgetgain.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.CalendarViewWeek
import androidx.compose.material.icons.outlined.Info
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import co.ke.foxlysoft.budgetgain.my_calendar.DayStatus
import co.ke.foxlysoft.budgetgain.my_calendar.MonthCalendar
import co.ke.foxlysoft.budgetgain.ui.Theme.BudgetGainTheme
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.todayIn
import kotlin.math.roundToInt
import kotlin.time.Clock
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.ke.foxlysoft.budgetgain.database.BudgetEntity
import co.ke.foxlysoft.budgetgain.database.TransactionEntity
import co.ke.foxlysoft.budgetgain.database.MpesaSmsEntity
import co.ke.foxlysoft.budgetgain.database.CategoryEntity
import co.ke.foxlysoft.budgetgain.utils.centsToString
import co.ke.foxlysoft.budgetgain.utils.amountToCents
import co.ke.foxlysoft.budgetgain.utils.isValidAmount
import co.ke.foxlysoft.budgetgain.utils.dateTimeMillisToString
import co.ke.foxlysoft.budgetgain.utils.getMerchantNameFromSms
import kotlinx.datetime.number
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DailyUsageScreen(
    dailyUsageScreenViewModel: DailyUsageScreenViewModel = koinViewModel(),
    categorizationViewModel: UncategorizedMpesaSmsScreenViewModel = koinViewModel()
) {
    val dayStatusMap by dailyUsageScreenViewModel.dayStatus.collectAsStateWithLifecycle()
    val budget by dailyUsageScreenViewModel.currentBudget.collectAsStateWithLifecycle()
    val selectedDayStatus by dailyUsageScreenViewModel.selectedDayStatus.collectAsStateWithLifecycle()
    val selectedDate by dailyUsageScreenViewModel.selectedDate.collectAsStateWithLifecycle()
    val selectedDayTransactions by dailyUsageScreenViewModel.selectedDayTransactions.collectAsStateWithLifecycle()
    val isCalendarLoading by dailyUsageScreenViewModel.isCalendarLoading.collectAsStateWithLifecycle()
    val dailyUsageInCents by dailyUsageScreenViewModel.dailyUsageInCents.collectAsStateWithLifecycle()
    val optimumDailyUsageInCents by dailyUsageScreenViewModel.optimumDailyUsageInCents.collectAsStateWithLifecycle()
    val uncategorizedTransactions by dailyUsageScreenViewModel.uncategorizedTransactions.collectAsStateWithLifecycle()
    val isUncategorizedLoading by dailyUsageScreenViewModel.isUncategorizedLoading.collectAsStateWithLifecycle()
    val selectableCategories by categorizationViewModel.selectableCategories.collectAsStateWithLifecycle()

    if (isCalendarLoading) {
        DailyUsageLoadingScreen()
    } else {
        DailyUsageScreenContent(
            budget = budget,
            statusByDate = dayStatusMap,
            selectedDayStatus = selectedDayStatus,
            dailyUsageInCents = dailyUsageInCents,
            optimumDailyUsageInCents = optimumDailyUsageInCents,
            selectedDate = selectedDate,
            selectedDayTransactions = selectedDayTransactions,
            uncategorizedTransactions = uncategorizedTransactions,
            isUncategorizedLoading = isUncategorizedLoading,
            onDayClick = dailyUsageScreenViewModel::selectDay,
            onDailyUsageChange = dailyUsageScreenViewModel::updateDailyUsage,
            onLoadUncategorized = dailyUsageScreenViewModel::loadUncategorizedTransactions,
            onIgnoreUncategorized = dailyUsageScreenViewModel::ignoreUncategorizedTransaction,
            selectableCategories = selectableCategories,
            onCategorySearch = categorizationViewModel::updateCategorySearchQuery,
            onCategorizeSingle = categorizationViewModel::categorizeSms,
            onCategorizeBulk = categorizationViewModel::categorizeBulkSms,
            onCategorizationComplete = dailyUsageScreenViewModel::refreshSelectedDay
        )
    }
}

@Composable
private fun DailyUsageLoadingScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Preparing your calendar…",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Calculating daily balances and spending",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

data class RemainingBudget (
    val type: String,
    val amount: Long,
    val percentage: Double,
    val icon: ImageVector
)

@Composable
fun DailyUsageScreenContent(
    budget: BudgetEntity,
    statusByDate: Map<LocalDate, DayStatus>,
    selectedDayStatus: DayStatus,
    dailyUsageInCents: Long = 30_000L,
    optimumDailyUsageInCents: Long = 0L,
    selectedDate: LocalDate? = null,
    selectedDayTransactions: List<DailyTransactionUiModel> = emptyList(),
    uncategorizedTransactions: List<MpesaSmsEntity> = emptyList(),
    isUncategorizedLoading: Boolean = false,
    onDayClick: (LocalDate) -> Unit = {},
    onDailyUsageChange: (Long) -> Unit = {},
    onLoadUncategorized: () -> Unit = {},
    onIgnoreUncategorized: (MpesaSmsEntity) -> Unit = {},
    selectableCategories: List<CategoryEntity> = emptyList(),
    onCategorySearch: (String) -> Unit = {},
    onCategorizeSingle: suspend (String, MpesaSmsEntity, Boolean) -> Unit = { _, _, _ -> },
    onCategorizeBulk: suspend (String, Set<Any>, Boolean) -> Unit = { _, _, _ -> },
    onCategorizationComplete: () -> Unit = {}
) {
    val splitBudgetYearMonth = budget.yearMonth.split("-")
    val budgetYear = if (budget.yearMonth.isNotEmpty()) splitBudgetYearMonth[0].toInt() else 2026
    val budgetMonth = if (budget.yearMonth.isNotEmpty()) splitBudgetYearMonth[1].toInt() else 2
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val referenceDate = selectedDate ?: if (today.year == budgetYear && today.month.number == budgetMonth) {
        today
    } else {
        LocalDate(budgetYear, budgetMonth, 1)
    }
    val dailyLimit = dailyUsageInCents
    val weeklyLimit = dailyLimit * 7
    val dailyRemaining = dailyLimit - (statusByDate[referenceDate]?.usedAmount ?: 0L)
    val weekStartDay = referenceDate.day - referenceDate.dayOfWeek.isoDayNumber + 1
    val weekEndDay = weekStartDay + 6
    val weeklyUsed = statusByDate
        .filterKeys { date -> date.day in weekStartDay..weekEndDay }
        .values
        .sumOf(DayStatus::usedAmount)
    val weeklyRemaining = weeklyLimit - weeklyUsed
    val monthlyRemaining = budget.budgetedAmount - budget.spentAmount
    val remainingBudgets = listOf(
        RemainingBudget(
            type = "Daily",
            amount = dailyRemaining,
            percentage = remainingPercentage(dailyRemaining, dailyLimit),
            icon = Icons.Default.CalendarToday
        ),
        RemainingBudget(
            type = "Weekly",
            amount = weeklyRemaining,
            percentage = remainingPercentage(weeklyRemaining, weeklyLimit),
            icon = Icons.Outlined.CalendarViewWeek
        ),
        RemainingBudget(
            type = "Monthly",
            amount = monthlyRemaining,
            percentage = remainingPercentage(monthlyRemaining, budget.budgetedAmount),
            icon = Icons.Default.CalendarMonth
        )
    )
    val progressColors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.secondary
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Daily Usage", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(12.dp))
        DailyUsageLimitCard(
            dailyUsageInCents = dailyUsageInCents,
            optimumDailyUsageInCents = optimumDailyUsageInCents,
            onDailyUsageChange = onDailyUsageChange
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Remaining budget",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Daily and weekly values follow the selected date",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            remainingBudgets.forEachIndexed { index, remainingBudget ->
                BudgetProgressCard(
                    remainingBudget = remainingBudget,
                    progressColor = progressColors[index],
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Card {
            MonthCalendar(
                year = budgetYear,
                month = budgetMonth,
                onDayClick = onDayClick,
                statusByDate = statusByDate,
                selectedDate = selectedDate
            )
        }
        AdditionalContentCue(hasSelectedDate = selectedDate != null)
        if (selectedDate != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Card {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DayAmountSummary(
                        label = "Brought forward",
                        amountInCents = selectedDayStatus.broughtForwardAmount,
                        modifier = Modifier.weight(1f)
                    )
                    VerticalDivider(modifier = Modifier.height(40.dp))
                    DayAmountSummary(
                        label = "Used",
                        amountInCents = selectedDayStatus.usedAmount,
                        modifier = Modifier.weight(1f)
                    )
                    VerticalDivider(modifier = Modifier.height(40.dp))
                    DayAmountSummary(
                        label = "Carried forward",
                        amountInCents = selectedDayStatus.carriedForwardAmount,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            SelectedDayTransactionsCard(
                selectedDate = selectedDate,
                transactions = selectedDayTransactions,
                uncategorizedTransactions = uncategorizedTransactions,
                isUncategorizedLoading = isUncategorizedLoading,
                onLoadUncategorized = onLoadUncategorized,
                onIgnoreUncategorized = onIgnoreUncategorized,
                selectableCategories = selectableCategories,
                onCategorySearch = onCategorySearch,
                onCategorizeSingle = onCategorizeSingle,
                onCategorizeBulk = onCategorizeBulk,
                onCategorizationComplete = onCategorizationComplete
            )
        }
    }
}

@Composable
private fun DailyUsageLimitCard(
    dailyUsageInCents: Long,
    optimumDailyUsageInCents: Long,
    onDailyUsageChange: (Long) -> Unit
) {
    var isEditDialogVisible by remember { mutableStateOf(false) }
    var dailyUsageInput by remember(dailyUsageInCents) {
        mutableStateOf(centsToString(dailyUsageInCents).replace(",", ""))
    }
    val isInputValid = isValidAmount(dailyUsageInput) &&
        (dailyUsageInput.toDoubleOrNull() ?: 0.0) > 0.0

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                DailyUsageInfoLabel(
                    label = "Daily usage",
                    information = "The amount added to your available balance every day. Tap edit to change it."
                )
                Text(
                    text = "KSh ${centsToString(dailyUsageInCents)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 1
                )
            }
            VerticalDivider(
                modifier = Modifier.height(38.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.22f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                DailyUsageInfoLabel(
                    label = "Optimum",
                    information = "Your average spend per elapsed day this month—the daily amount that would fully cover usage without overspending."
                )
                Text(
                    text = if (optimumDailyUsageInCents > 0) {
                        "KSh ${centsToString(optimumDailyUsageInCents)}"
                    } else {
                        "No usage yet"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 1
                )
            }
            IconButton(
                modifier = Modifier.size(36.dp),
                onClick = {
                    dailyUsageInput = centsToString(dailyUsageInCents).replace(",", "")
                    isEditDialogVisible = true
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit daily usage",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }

    if (isEditDialogVisible) {
        AlertDialog(
            onDismissRequest = { isEditDialogVisible = false },
            title = { Text("Set daily usage") },
            text = {
                Column {
                    Text("This amount is added to your available balance each day.")
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = dailyUsageInput,
                        onValueChange = { value ->
                            if (value.isEmpty() || value.matches(Regex("^\\d*(\\.\\d{0,2})?$"))) {
                                dailyUsageInput = value
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Daily usage") },
                        prefix = { Text("KSh ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        isError = dailyUsageInput.isNotEmpty() && !isInputValid,
                        supportingText = if (dailyUsageInput.isNotEmpty() && !isInputValid) {
                            { Text("Enter an amount greater than zero") }
                        } else {
                            null
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = isInputValid,
                    onClick = {
                        onDailyUsageChange(amountToCents(dailyUsageInput))
                        isEditDialogVisible = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { isEditDialogVisible = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun DailyUsageInfoLabel(label: String, information: String) {
    var isInformationVisible by remember { mutableStateOf(false) }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
        )
        Box {
            IconButton(
                modifier = Modifier.size(24.dp),
                onClick = { isInformationVisible = true }
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "About $label",
                    modifier = Modifier.size(15.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                )
            }
            DropdownMenu(
                expanded = isInformationVisible,
                onDismissRequest = { isInformationVisible = false }
            ) {
                Text(
                    text = information,
                    modifier = Modifier.width(260.dp).padding(12.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

private fun remainingPercentage(remainingAmount: Long, totalAmount: Long): Double {
    if (totalAmount <= 0) return 0.0
    return remainingAmount.coerceIn(0, totalAmount) * 100.0 / totalAmount
}

@Composable
private fun AdditionalContentCue(hasSelectedDate: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (hasSelectedDate) "Daily details below" else "Select a day for more details",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun BudgetProgressCard(
    remainingBudget: RemainingBudget,
    progressColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = progressColor.copy(alpha = 0.10f)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = remainingBudget.icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = progressColor
                )
                Text(
                    text = remainingBudget.type,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { (remainingBudget.percentage / 100).toFloat() },
                    modifier = Modifier.size(64.dp),
                    color = progressColor,
                    trackColor = progressColor.copy(alpha = 0.18f),
                    strokeWidth = 7.dp
                )
                Text(
                    text = "${remainingBudget.percentage.roundToInt()}%",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "KSh ${centsToString(remainingBudget.amount)}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (remainingBudget.amount < 0) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                maxLines = 1
            )
            Text(
                text = if (remainingBudget.amount < 0) "over budget" else "remaining",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DayAmountSummary(
    label: String,
    amountInCents: Long,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "KSh ${centsToString(amountInCents)}",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun SelectedDayTransactionsCard(
    selectedDate: LocalDate,
    transactions: List<DailyTransactionUiModel>,
    uncategorizedTransactions: List<MpesaSmsEntity>,
    isUncategorizedLoading: Boolean,
    onLoadUncategorized: () -> Unit,
    onIgnoreUncategorized: (MpesaSmsEntity) -> Unit,
    selectableCategories: List<CategoryEntity>,
    onCategorySearch: (String) -> Unit,
    onCategorizeSingle: suspend (String, MpesaSmsEntity, Boolean) -> Unit,
    onCategorizeBulk: suspend (String, Set<Any>, Boolean) -> Unit,
    onCategorizationComplete: () -> Unit
) {
    var selectedFilter by remember(selectedDate) {
        mutableStateOf(TransactionFilter.CATEGORIZED)
    }
    var selectedSmsIds by remember(selectedDate) { mutableStateOf(emptySet<Long>()) }
    var smsForCategorization by remember(selectedDate) {
        mutableStateOf(emptyList<MpesaSmsEntity>())
    }
    val visibleItemCount = when (selectedFilter) {
        TransactionFilter.CATEGORIZED -> transactions.size
        TransactionFilter.UNCATEGORIZED -> uncategorizedTransactions.size
        TransactionFilter.ALL -> transactions.size + uncategorizedTransactions.size
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Transactions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = selectedDate.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "$visibleItemCount ${if (visibleItemCount == 1) "item" else "items"}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                TransactionFilter.entries.forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = {
                            selectedFilter = filter
                            if (filter != TransactionFilter.CATEGORIZED) {
                                onLoadUncategorized()
                            }
                        },
                        label = { Text(filter.label) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (isUncategorizedLoading && selectedFilter != TransactionFilter.CATEGORIZED) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Loading uncategorized transactions…")
                }
            } else if (visibleItemCount == 0) {
                Text(
                    text = when (selectedFilter) {
                        TransactionFilter.CATEGORIZED -> "No categorized transactions for this day"
                        TransactionFilter.UNCATEGORIZED -> "No uncategorized transactions for this day"
                        TransactionFilter.ALL -> "No transactions recorded for this day"
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                if (selectedFilter != TransactionFilter.UNCATEGORIZED) {
                    transactions.forEach { transactionItem ->
                        TransactionTile(
                            transactionItem = transactionItem,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
                if (selectedFilter != TransactionFilter.CATEGORIZED) {
                    if (selectedSmsIds.isEmpty()) {
                        Text(
                            text = "Tap a transaction for options • Hold to select multiple",
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${selectedSmsIds.size} selected",
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                            TextButton(onClick = { selectedSmsIds = emptySet() }) {
                                Text("Clear")
                            }
                            TextButton(
                                onClick = {
                                    smsForCategorization = uncategorizedTransactions.filter {
                                        it.id in selectedSmsIds
                                    }
                                }
                            ) {
                                Text("Categorize selected")
                            }
                        }
                    }
                    uncategorizedTransactions.forEach { sms ->
                        UncategorizedTransactionTile(
                            sms = sms,
                            isSelected = sms.id in selectedSmsIds,
                            isSelectionMode = selectedSmsIds.isNotEmpty(),
                            onSelectionChange = { isSelected ->
                                selectedSmsIds = if (isSelected) {
                                    selectedSmsIds + sms.id
                                } else {
                                    selectedSmsIds - sms.id
                                }
                            },
                            onCategorize = { smsForCategorization = listOf(sms) },
                            onIgnore = { onIgnoreUncategorized(sms) },
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }

    if (smsForCategorization.isNotEmpty()) {
        val firstSms = smsForCategorization.first()
        CategorizationBottomSheet(
            itemCount = smsForCategorization.size,
            merchantLabel = if (smsForCategorization.size > 1) {
                "${smsForCategorization.size} selected items"
            } else {
                getMerchantNameFromSms(firstSms)
            },
            selectableCategories = selectableCategories,
            onCategorySearch = onCategorySearch,
            onDismiss = { smsForCategorization = emptyList() },
            onSubmit = { categoryName, categorizeSimilar ->
                if (smsForCategorization.size == 1) {
                    onCategorizeSingle(categoryName, firstSms, categorizeSimilar)
                } else {
                    onCategorizeBulk(
                        categoryName,
                        smsForCategorization.map { it.id }.toSet(),
                        categorizeSimilar
                    )
                }
            },
            onSuccess = {
                smsForCategorization = emptyList()
                selectedSmsIds = emptySet()
                onCategorizationComplete()
            },
            onError = {}
        )
    }
}

private enum class TransactionFilter(val label: String) {
    CATEGORIZED("Categorized"),
    UNCATEGORIZED("Uncategorized"),
    ALL("All")
}

@Composable
private fun TransactionTile(
    transactionItem: DailyTransactionUiModel,
    modifier: Modifier = Modifier
) {
    val transaction = transactionItem.transaction
    val merchantName = transactionItem.merchantName

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.38f)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = merchantName
                        .trim()
                        .firstOrNull()
                        ?.uppercaseChar()
                        ?.toString()
                        ?: "T",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = merchantName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                if (transaction.ref.isNotBlank()) {
                    Text(
                        text = "#${transaction.ref}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = transaction.timestamp.substringAfter(' ', transaction.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "KSh ${centsToString(transaction.amount)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                TransactionStatusIcon(
                    icon = Icons.Default.CheckCircle,
                    color = MaterialTheme.colorScheme.primary,
                    meaning = "Categorized transaction"
                )
            }
        }
    }
}

@Composable
private fun TransactionStatusIcon(
    icon: ImageVector,
    color: Color,
    meaning: String
) {
    var isMeaningVisible by remember { mutableStateOf(false) }

    Box {
        IconButton(
            modifier = Modifier.size(28.dp),
            onClick = { isMeaningVisible = true }
        ) {
            Icon(
                imageVector = icon,
                contentDescription = meaning,
                modifier = Modifier.size(18.dp),
                tint = color
            )
        }
        DropdownMenu(
            expanded = isMeaningVisible,
            onDismissRequest = { isMeaningVisible = false }
        ) {
            Text(
                text = meaning,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun UncategorizedTransactionTile(
    sms: MpesaSmsEntity,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onSelectionChange: (Boolean) -> Unit,
    onCategorize: () -> Unit,
    onIgnore: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isMenuExpanded by remember { mutableStateOf(false) }
    val merchantName = getMerchantNameFromSms(sms).ifBlank { "Unknown merchant" }

    Box(modifier = modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClickLabel = if (isSelectionMode) "Toggle selection" else "Show transaction options",
                    onLongClickLabel = "Select transaction",
                    onClick = {
                        if (isSelectionMode) onSelectionChange(!isSelected) else isMenuExpanded = true
                    },
                    onLongClick = { onSelectionChange(!isSelected) }
                ),
            colors = CardDefaults.cardColors(
                containerColor = if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.tertiaryContainer
                }
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.tertiary,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Selected",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(
                            text = merchantName.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onTertiary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = merchantName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                    Text(
                        text = "#${sms.ref}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.72f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = dateTimeMillisToString(sms.dateTime).substringAfter('T'),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.72f),
                        maxLines = 1
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "KSh ${centsToString(sms.amount)}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    TransactionStatusIcon(
                        icon = Icons.Default.Warning,
                        color = MaterialTheme.colorScheme.tertiary,
                        meaning = "Uncategorized transaction"
                    )
                }
            }
        }
        DropdownMenu(
            expanded = isMenuExpanded,
            onDismissRequest = { isMenuExpanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Categorize") },
                onClick = {
                    isMenuExpanded = false
                    onCategorize()
                }
            )
            DropdownMenuItem(
                text = { Text("Ignore") },
                onClick = {
                    isMenuExpanded = false
                    onIgnore()
                }
            )
        }
    }
}

@Preview(name = "Daily usage - Light")
@Composable
fun LightDailyUsageScreenPreview() {
    DailyUsageScreenPreviewContent(darkTheme = false)
}

@Preview(name = "Daily usage - Dark")
@Composable
fun DarkDailyUsageScreenPreview() {
    DailyUsageScreenPreviewContent(darkTheme = true)
}

@Composable
private fun DailyUsageScreenPreviewContent(darkTheme: Boolean) {
    val budget = BudgetEntity(
        yearMonth = "2026-02",
        isActive = true,
        budgetedAmount = 3_000_000,
        spentAmount = 1_125_000
    )

    val firstDate = LocalDate(2026,2,1)

    val firstDayStatus = DayStatus(
        broughtForwardAmount = 10000,
        isMovedForward = true,
        carriedForwardAmount = 20000,
        isUsed = true,
        usedAmount = 30000,
        isOverUsed = true,
        overUsedAmount = 30000
    )

    val statusByDate: Map<LocalDate, DayStatus> = mapOf(
        firstDate to firstDayStatus
    )
    val transactions = listOf(
        DailyTransactionUiModel(
            merchantName = "Naivas Supermarket",
            transaction = TransactionEntity(
                ref = "QH82K1",
                amount = 12_450,
                timestamp = "2026-02-01 10:24"
            )
        ),
        DailyTransactionUiModel(
            merchantName = "Java House",
            transaction = TransactionEntity(
                ref = "QH82K2",
                amount = 3_850,
                timestamp = "2026-02-01 14:05"
            )
        )
    )

    BudgetGainTheme(darkTheme = darkTheme) {
        Surface {
            DailyUsageScreenContent(
                budget = budget,
                statusByDate = statusByDate,
                selectedDayStatus = firstDayStatus,
                dailyUsageInCents = 30_000,
                optimumDailyUsageInCents = 24_650,
                selectedDate = firstDate,
                selectedDayTransactions = transactions
            )
        }
    }
}
