package co.ke.foxlysoft.budgetgain.ui

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CalendarViewMonth
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Shop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.ke.foxlysoft.budgetgain.database.AccountEntity
import co.ke.foxlysoft.budgetgain.database.CategoryEntity
import co.ke.foxlysoft.budgetgain.database.TransactionEntity
import co.ke.foxlysoft.budgetgain.repos.MerchantSummary
import co.ke.foxlysoft.budgetgain.ui.Theme.BudgetGainTheme
import co.ke.foxlysoft.budgetgain.ui.Theme.Purple600
import co.ke.foxlysoft.budgetgain.ui.charts.MonthLineChart
import co.ke.foxlysoft.budgetgain.ui.components.BGPaginatedList
import co.ke.foxlysoft.budgetgain.ui.components.BGainPaginationController
import co.ke.foxlysoft.budgetgain.utils.centsToString
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.yearMonth
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.parameter.parametersOf

@OptIn(KoinExperimentalAPI::class)
@Composable
fun CategoryDetailsScreen(
    categoryId: Long,
    categoryDetailsScreenViewModel: CategoryDetailsScreenViewModel = koinViewModel(parameters = { parametersOf(categoryId) }),
    onNavigateBack: () -> Unit,
    onOpenConfirmSnackbar: (msg: String, actionLabel: String, onConfirm: () -> Unit) -> Unit
){
    val category = categoryDetailsScreenViewModel.currentCategory.collectAsState().value
    var merchantSummary by remember { mutableStateOf(emptyList<MerchantSummary>()) }
    var dailyData by remember { mutableStateOf(emptyList<Pair<Int, Long>>()) }

    val scope = rememberCoroutineScope()
    scope.launch {
        merchantSummary = categoryDetailsScreenViewModel.getMerchantSummaryForCategory()
        dailyData = categoryDetailsScreenViewModel.getCurrentMonthDailySpendByCategory()
    }


    CategoryDetailsContent(
        category = category,
        merchantsSummary = merchantSummary,
        dailyData = dailyData,
        onGetMerchantAccount = categoryDetailsScreenViewModel::getMerchantAccount,
        onDeleteTransaction = categoryDetailsScreenViewModel::deleteTransaction,
        onGetCategoryTransactions = categoryDetailsScreenViewModel::getCategoryTransactions,
        onOpenConfirmSnackbar
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailsContent(
    category: CategoryEntity,
    merchantsSummary: List<MerchantSummary> = emptyList(),
    dailyData: List<Pair<Int, Long>> = emptyList(),
    onGetMerchantAccount: suspend (TransactionEntity) -> AccountEntity,
    onDeleteTransaction: suspend (TransactionEntity) -> Unit,
    onGetCategoryTransactions: suspend (Int, Int) -> List<TransactionEntity>,
    onOpenConfirmSnackbar: (msg: String, actionLabel: String, onConfirm: () -> Unit) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    val tabs = listOf("Transactions", "Merchants", "Charts")
    val tabIcons = listOf(Icons.Default.Receipt, Icons.Outlined.Shop, Icons.Default.BarChart)
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val selectedTabIndex = remember { derivedStateOf { pagerState.currentPage } }

    val merchantSummaryListState = rememberLazyListState()

    var isTrackDropdownExpanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier.fillMaxSize()
            .padding(16.dp)
    ){
        Text(text= category.name, style = MaterialTheme.typography.headlineLarge)
        Text(text = "Remaining: Ksh${centsToString(category.amount - category.spentAmount)}")
        Spacer(modifier = Modifier.height(8.dp))

        PrimaryTabRow(
            selectedTabIndex = selectedTabIndex.value,
            modifier = Modifier.fillMaxWidth()
        ) {
            for ((idx, tab) in tabs.withIndex()) {
                Tab(
                    selected = selectedTabIndex.value == idx,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(idx)
                        }
                    },
                    icon = { Icon(imageVector = tabIcons[idx], contentDescription = "Add") },
                    text = { Text(text = tab) }
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
            ,
            verticalAlignment = Alignment.Top
        ) {
            if (selectedTabIndex.value == 0) {
                val paginationController = remember { BGainPaginationController() }
                BGPaginatedList(
                    onGetKey = { it.id },
                    onGetItem = {
                        TransactionItem(
                            onGetMerchantAccount = onGetMerchantAccount,
                            it,
                            onDelete = {
                                // TODO: add a confirmation dialog
                                onOpenConfirmSnackbar(
                                    "Are you sure you want to delete?",
                                    "Confirm",
                                    {
                                        // Perform the delete action
                                        coroutineScope.launch {
                                            onDeleteTransaction(it)
                                            paginationController.refreshAllPages()
                                        }
                                    }
                                )

                            })
                    },
                    onGetItems = { limit, offset ->
                        onGetCategoryTransactions(limit, offset)
                    },
                    controller = paginationController
                )
            }
            if (selectedTabIndex.value == 1) {
                LazyColumn(
                    state = merchantSummaryListState,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    items(
                        merchantsSummary.size,
                        key = { merchantsSummary[it].merchantName }
                    ){
                        MerchantSummaryItem(merchantSummary = merchantsSummary[it])
                    }
                }
            }
            if (selectedTabIndex.value == 2) {
                Column {
                    MonthSpendingChart(
                        title = "Category - ${category.name}",
                        dailyData = dailyData
                    )
                }
            }
            if (selectedTabIndex.value == 3) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row {
                        Spacer(modifier = Modifier.weight(1f))
                        ExposedDropdownMenuBox(
                            modifier = Modifier.weight(1f),
                            expanded = isTrackDropdownExpanded,
                            onExpandedChange = { isTrackDropdownExpanded = !isTrackDropdownExpanded}
                        ) {
                            TextField(
                                value = category.trackMode ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Track Mode") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isTrackDropdownExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Card {
                        Column(
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Text(text = "Track", style = MaterialTheme.typography.titleMedium)
                            Text("Select either daily/weekly track to view and analyze your spending habits")
                        }
                    }
                }
            }
        }

    }
}

@Composable
fun TransactionItem(
    onGetMerchantAccount: suspend (TransactionEntity) -> AccountEntity,
    transaction: TransactionEntity,
    onDelete: () -> Unit
) {
    // State to track the expanded state of the menu
    var menuExpanded by remember { mutableStateOf(false) }

    var merchantAccount by remember { mutableStateOf(AccountEntity(merchantName = "Sample Merchant")) }

    LaunchedEffect(key1 = Unit) {
        merchantAccount = onGetMerchantAccount(transaction)
    }

    Card{
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            var description = transaction.description
            if (transaction.description.length > 10) {
                description = description.take(10) +"..."
            }
            Column(modifier = Modifier.weight(6f)) {
                Text(text = "#${transaction.ref}", style = TextStyle(fontSize = 12.sp))
                Text(text= merchantAccount.merchantName)
                Text(text= description, style = TextStyle(fontSize = 14.sp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column (modifier = Modifier.weight(3f)) {
                Text(text = transaction.timestamp, style = TextStyle(fontSize = 12.sp))
                Text(text = "Ksh${centsToString(transaction.amount)}", style = MaterialTheme.typography.bodyLarge)
            }
            Box(modifier = Modifier.weight(1f)) {
                IconButton(onClick = {
                    menuExpanded = true
                }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Menu"
                    )
                }
                // Dropdown menu
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(onClick = {
                        onDelete()
                        menuExpanded = false
                    },
                        text = {
                            Text("Delete")
                        })
                }
            }
        }
    }
}

@Composable
fun MerchantSummaryItem(
    merchantSummary: MerchantSummary
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Text(text = merchantSummary.merchantName, style = MaterialTheme.typography.titleMedium)
            Row {
                Text(text = "Spent: ${centsToString(merchantSummary.spentAmount)}")
                Spacer(modifier = Modifier.weight(1f))
                Text("${merchantSummary.transactionCount} transactions")
            }

        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
fun MonthSpendingChart(title: String? = "", dailyData: List<Pair<Int, Long>>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title ?: "Month Spending Chart",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            val currentDate = Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date
            val daysInMonth = currentDate.yearMonth.numberOfDays
            val dailyAmountByDay = dailyData.associate { it.first to it.second }
            val seriesValues = (1..daysInMonth).map { day ->
                (dailyAmountByDay[day] ?: 0L).toDouble() / 100
            }

            MonthLineChart(
                "2026-02",
                dailyData
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CategoryDetailsContentPreview() {
    val sampleCategory = CategoryEntity(
        id = 1,
        budgetId = 1,
        name = "Groceries",
        amount = 150_000,
        spentAmount = 62_500
    )
    val sampleTransactions = listOf(
        TransactionEntity(
            id = 1,
            ref = "TXN-001",
            description = "Whole foods market",
            categoryId = 1,
            amount = 12_500,
            timestamp = "2026-02-07 09:15"
        ),
        TransactionEntity(
            id = 2,
            ref = "TXN-002",
            description = "Local grocery",
            categoryId = 1,
            amount = 50_000,
            timestamp = "2026-02-06 18:42"
        )
    )
    val sampleMerchantSummaries = listOf(
        MerchantSummary(merchantName = "Sample Merchant", spentAmount = 12_500, transactionCount = 2),
        MerchantSummary(merchantName = "Sample Merchant 2", spentAmount = 50_000, transactionCount = 1)
    )

    BudgetGainTheme {
        Surface {
            CategoryDetailsContent(
                category = sampleCategory,
                merchantsSummary = sampleMerchantSummaries,
                onGetMerchantAccount = { AccountEntity(merchantName = "Sample Merchant") },
                onDeleteTransaction = {},
                onGetCategoryTransactions = { _, _ -> sampleTransactions },
                onOpenConfirmSnackbar = { _, _, _ -> }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TransactionItemPreview() {
    val sampleTransaction = TransactionEntity(
        id = 1,
        ref = "TXN-003",
        description = "Neighborhood market",
        categoryId = 1,
        amount = 7_250,
        timestamp = "2026-02-07 12:30"
    )

    BudgetGainTheme {
        Surface {
            TransactionItem(
                onGetMerchantAccount = { AccountEntity(merchantName = "Sample Merchant") },
                transaction = sampleTransaction,
                onDelete = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DarkTransactionItemPreview() {
    val sampleTransaction = TransactionEntity(
        id = 1,
        ref = "TXN-003",
        description = "Neighborhood market",
        categoryId = 1,
        amount = 7_250,
        timestamp = "2026-02-07 12:30"
    )

    BudgetGainTheme(darkTheme = true) {
        Surface {
            TransactionItem(
                onGetMerchantAccount = { AccountEntity(merchantName = "Sample Merchant") },
                transaction = sampleTransaction,
                onDelete = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MonthSpendingChartPreview() {
    val sampleCategory = CategoryEntity(
        id = 1,
        budgetId = 1,
        name = "Groceries",
        amount = 150_000,
        spentAmount = 62_500
    )
    val sampleDailyData = listOf(
        1 to 2_500L,
        2 to 7_500L,
        3 to 1_200L,
        4 to 9_000L,
        5 to 3_400L,
        6 to 5_800L,
        7 to 6_100L,
        20 to 5_000L
    )

    BudgetGainTheme {
        Surface {
            MonthSpendingChart(
                title = "Category - ${sampleCategory.name}",
                dailyData = sampleDailyData
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DarkMonthSpendingChartPreview() {
    val sampleCategory = CategoryEntity(
        id = 1,
        budgetId = 1,
        name = "Groceries",
        amount = 150_000,
        spentAmount = 62_500
    )
    val sampleDailyData = listOf(
        1 to 2_500L,
        2 to 7_500L,
        3 to 1_200L,
        4 to 9_000L,
        5 to 3_400L,
        6 to 5_800L,
        7 to 6_100L,
        20 to 5_000L
    )

    BudgetGainTheme(darkTheme = true) {
        Surface {
            MonthSpendingChart(
                title = "Category - ${sampleCategory.name}",
                dailyData = sampleDailyData
            )
        }
    }
}
