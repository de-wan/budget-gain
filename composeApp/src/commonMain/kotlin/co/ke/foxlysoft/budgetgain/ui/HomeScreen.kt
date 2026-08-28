package co.ke.foxlysoft.budgetgain.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.ke.foxlysoft.budgetgain.database.BudgetEntity
import co.ke.foxlysoft.budgetgain.database.CategoryEntity
import co.ke.foxlysoft.budgetgain.database.MpesaSmsEntity
import co.ke.foxlysoft.budgetgain.navigation.Screens
import co.ke.foxlysoft.budgetgain.shared.AdMobBanner
import co.ke.foxlysoft.budgetgain.shared.PermissionLaucher
import co.ke.foxlysoft.budgetgain.shared.SmsReader
import co.ke.foxlysoft.budgetgain.ui.Theme.BudgetGainTheme
import co.ke.foxlysoft.budgetgain.ui.Theme.Green700
import co.ke.foxlysoft.budgetgain.ui.Theme.Orange500
import co.ke.foxlysoft.budgetgain.ui.Theme.Purple400
import co.ke.foxlysoft.budgetgain.ui.components.BGPaginatedList
import co.ke.foxlysoft.budgetgain.ui.components.BGainOutlineField
import co.ke.foxlysoft.budgetgain.utils.ErrorStatus
import co.ke.foxlysoft.budgetgain.utils.MpesaSmsTypes
import co.ke.foxlysoft.budgetgain.utils.QueryState
import co.ke.foxlysoft.budgetgain.utils.centsToString
import co.ke.foxlysoft.budgetgain.utils.smsParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(KoinExperimentalAPI::class)
@Composable
fun HomeScreen(
     homeScreenViewModel: HomeScreenViewModel = koinViewModel(),
    onNavigate: (String) -> Unit,
) {
    // PENDING, NO_CURRENT_BUDGET, COMPLETE, ERROR

    val currentBudget = homeScreenViewModel.currentBudget.collectAsState().value
    val pageState = homeScreenViewModel.pageState.collectAsStateWithLifecycle()


    HomeContent(
        pageState = pageState.value,
        onNavigate = onNavigate,
        currentBudget = currentBudget,
        onActivateBudget = homeScreenViewModel::activateBudget,
        onReplenishBudget = homeScreenViewModel::replenishBudget,
        onDeleteCategory = homeScreenViewModel::deleteCategory,
        onGetBudgetCategories = homeScreenViewModel::getBudgetCategories,
        onGetAllBudgets = homeScreenViewModel::getAllBudgets,
        onUpsertMpesaSms = homeScreenViewModel::upsertMpesaSms,
        onGetCurrentMonthDailySpendByCategory = homeScreenViewModel::getCurrentMonthDailySpend
    )

}

@Composable
fun HomeContent(
    pageState: QueryState,
    onNavigate: (String) -> Unit,
    currentBudget: BudgetEntity,
    onActivateBudget: (Long) -> Unit = {},
    onReplenishBudget: suspend (String) -> Unit = {},
    onDeleteCategory: suspend (CategoryEntity) -> Unit = {},
    onGetBudgetCategories: suspend (limit: Int, offset: Int) -> List<CategoryEntity> = { _, _ -> emptyList() },
    onGetAllBudgets: suspend () -> List<BudgetEntity> = {emptyList()},
    onUpsertMpesaSms: suspend (MpesaSmsEntity) -> Unit = {},
    onGetCurrentMonthDailySpendByCategory: suspend () -> List<Pair<Int, Long>> = { emptyList() }
) {
    val coroutineScope = rememberCoroutineScope()

    val tabs = listOf("Categories", "Charts", "Info")
    val tabIcons = listOf(Icons.Default.Category, Icons.Default.BarChart, Icons.Outlined.Info)
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val selectedTabIndex = remember { derivedStateOf { pagerState.currentPage } }

    var showSelectBudgetDialog by remember { mutableStateOf(false) }
    var selectableBudgets by remember { mutableStateOf(emptyList<BudgetEntity>()) }

    var showReplenishBudget by remember { mutableStateOf(false) }
    var replenishAmount by remember { mutableStateOf("") }
    var replenishAmountErrorStatus by remember { mutableStateOf(ErrorStatus(isError = false)) }

    var dailyData by remember { mutableStateOf(emptyList<Pair<Int, Long>>()) }

    var isPermissionGranted by remember {
        mutableStateOf(false)
    }
    PermissionLaucher(
        onPermissionGranted = {
            isPermissionGranted = true
            println("Permission granted")
        },
        onPermissionDenied = {
            isPermissionGranted = false
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        when (pageState) {
            QueryState.LOADING -> {
                CircularProgressIndicator()
            }
            QueryState.NO_RESULTS -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxWidth().weight(1f)
                ) {
                    Column{
                        Text(text = "Welcome! to Budget Gain.")
                        Text(text = "To get started, create a budget.")
                        Button(onClick = {
                            onNavigate(Screens.CreateBudgetScreen.route)
                        }) {
                            Text(text = "Create Budget")
                        }
                    }

                }
            }
            QueryState.COMPLETE -> {
                if (isPermissionGranted) {
                    println("fetching sms")
                    coroutineScope.launch {
                        withContext(Dispatchers.Default) {
                            dailyData = onGetCurrentMonthDailySpendByCategory()

                            val splitYearMonth = currentBudget.yearMonth.split("-").map{ it.toInt() }
                            val yearMonth = YearMonth(splitYearMonth[0], Month(splitYearMonth[1]))

                            val from = yearMonth.firstDay.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
                            val lastDay = yearMonth.lastDay
                            val to = lastDay.plus(1, DateTimeUnit.DAY)
                                .atStartOfDayIn(TimeZone.currentSystemDefault())
                                .toEpochMilliseconds() - 1

                            val rawMpesaSms = SmsReader().getMpesaSms(from, to)

                            for (rawSms in rawMpesaSms) {
                                val sms = smsParser(rawSms)
                                if (sms.smsType == MpesaSmsTypes.UNKNOWN) {
                                    println("Unknown sms: $rawSms")
                                    continue
                                }

                                val mpesaSmsEntity = MpesaSmsEntity(
                                    smsType = sms.smsType,
                                    ref = sms.ref,
                                    amount = sms.amount,
                                    dateTime = sms.dateTime,
                                    subjectPrimaryIdentifierType = sms.subjectPrimaryIdentifierType,
                                    subjectPrimaryIdentifier = sms.subjectPrimaryIdentifier,
                                    subjectSecondaryIdentifierType = sms.subjectSecondaryIdentifierType,
                                    subjectSecondaryIdentifier = sms.subjectSecondaryIdentifier,
                                    cost = sms.cost,
                                    balance = sms.balance,
                                    isIgnored = false,
                                )

                                onUpsertMpesaSms(mpesaSmsEntity)
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth().weight(1f)
                ){
                    Row(
                        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primaryContainer),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.width(8.dp))
                        ElevatedButton(
                            onClick = {
                                coroutineScope.launch {
                                    selectableBudgets = onGetAllBudgets()
//                                    budgetToActivate = currentBudget.id
                                }
                                showSelectBudgetDialog = true
                            }
                        ) {
                            Text(
                                text = currentBudget.yearMonth,
                                fontSize = 18.sp,
                            )
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        VerticalDivider(
                            modifier = Modifier.height(30.dp)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(onClick = { onNavigate(Screens.DailyUsage.route) }) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Daily usage"
                            )
                            Text("Daily")
                        }
                        Text(text = "Bal: ")
                        Text(centsToString(currentBudget.budgetedAmount - currentBudget.spentAmount),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    PrimaryTabRow(
                        selectedTabIndex = selectedTabIndex.value,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        for ((idx, tab) in tabs.withIndex()) {
                            Tab(
                                selected = selectedTabIndex.value == idx,
                                onClick = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(idx)
                                    }
                                },
                                icon = { Icon(imageVector = tabIcons[idx], contentDescription = "Add") },
                                text = { Text(text = tab) }
                            )
                        }
                    }

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(16.dp, 0.dp)
                        ,
                        verticalAlignment = Alignment.Top
                    ) {
                        if (selectedTabIndex.value == 0) {
                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Card(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth().padding(8.dp)
                                    ){
                                        Text(centsToString(currentBudget.initialBalance - currentBudget.budgetedAmount),
                                            style = MaterialTheme.typography.titleMedium)
                                        Text("Ready for categorization")
                                        Row {
                                            Spacer(modifier = Modifier.weight(1f))
                                            Button(onClick = {
                                                onNavigate(
                                                    Screens.AddCategoryScreen.createRoute(
                                                        currentBudget.id
                                                    )
                                                )
                                            }) {
                                                Text("Add Category")
                                            }
                                        }

                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                HorizontalDivider()
                                Spacer(modifier = Modifier.height(12.dp))

                                Column {
                                    BGPaginatedList(
                                        onGetKey = { it.id },
                                        onGetItem = {
                                            CategoryItem(
                                                it,
                                                onNavigate = onNavigate,
                                                onDeleteCategory = {
                                                    coroutineScope.launch {
                                                        onDeleteCategory(it)
                                                    }
                                                })
                                        },
                                        onGetItems = { limit, offset ->
                                            onGetBudgetCategories(limit, offset)
                                        }
                                    )
                                }
                            }
                        }
                        else if (selectedTabIndex.value == 1) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Column {
                                MonthSpendingChart(
                                    title = "Budget Spending",
                                    dailyData = dailyData
                                )
                            }
                        }
                        else {
                            Column {
                                Text("Budget Info", style = MaterialTheme.typography.headlineMedium)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Initial Balance", style = MaterialTheme.typography.titleMedium)
                                Text(centsToString(currentBudget.initialBalance))
                                HorizontalDivider()
                                Text("Budget Amount", style = MaterialTheme.typography.titleMedium)
                                Text(centsToString(currentBudget.budgetedAmount))
                                HorizontalDivider()
                                Text("Unbudgeted Amount", style = MaterialTheme.typography.titleMedium)
                                Text(centsToString(currentBudget.initialBalance - currentBudget.budgetedAmount))
                                HorizontalDivider()
                                Text("Spent Amount", style = MaterialTheme.typography.titleMedium)
                                Text(centsToString(currentBudget.spentAmount))
                                HorizontalDivider()
                                Text("Budget Balance", style = MaterialTheme.typography.titleMedium)
                                Text(centsToString(currentBudget.budgetedAmount - currentBudget.spentAmount))
                                HorizontalDivider()
                            }

                        }
                    }
                }

                if (showSelectBudgetDialog) {
                    Dialog(
                        onDismissRequest = {
                            showSelectBudgetDialog = false
                        }
                    ) {
                        Card {
                            Column(
                                modifier = Modifier.padding(24.dp).width(300.dp)
                            ) {
                                Text("Select Budget", style = MaterialTheme.typography.headlineLarge)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Tap a budget to activate it")
                                Spacer(modifier = Modifier.height(16.dp))
                                for (budget in selectableBudgets) {
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .height(56.dp)
                                            .selectable(
                                                selected = (currentBudget.id == budget.id),
                                                onClick = {
                                                onActivateBudget(budget.id)
                                                showSelectBudgetDialog = false
                                            },
                                                role = Role.RadioButton
                                            )
                                            .padding(horizontal = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = currentBudget.id == budget.id,
                                            onClick = null
                                        )
                                        Text(
                                            budget.yearMonth,
                                            style = MaterialTheme.typography.bodyLarge,
                                            modifier = Modifier.padding(start = 16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                                HorizontalDivider()
                                Spacer(modifier = Modifier.height(8.dp))
                                // Replenish item inside the selector
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .height(56.dp)
                                        .selectable(
                                            selected = false,
                                            onClick = {
                                                showSelectBudgetDialog = false
                                                replenishAmount = ""
                                                showReplenishBudget = true
                                            },
                                            role = Role.Button
                                        )
                                        .padding(horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Replenish"
                                    )
                                    Text(
                                        "Replenish Budget",
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.padding(start = 16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                if (showReplenishBudget) {
                    Dialog(
                        onDismissRequest = {
                            showReplenishBudget = false
                        }
                    ) {
                        Card {
                            Column(
                                modifier = Modifier.padding(24.dp).width(300.dp)
                            ) {
                                Text("Replenish Budget", style = MaterialTheme.typography.headlineLarge)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("This increases budget amount.")
                                Text("New amount = current amount + amount entered", style = MaterialTheme.typography.bodySmall)
                                Spacer(modifier = Modifier.height(16.dp))
                                BGainOutlineField(
                                    Value = replenishAmount,
                                    onValueChange = { replenishAmount = it },
                                    labelStr = "Amount",
                                    validator = {
                                        replenishAmountErrorStatus = ErrorStatus(isError = false)
                                        if (it.isEmpty()){
                                            replenishAmountErrorStatus = ErrorStatus(isError = true, errorMsg = "Replenish Amount is required")
                                            return@BGainOutlineField
                                        }
                                        if (it.toDoubleOrNull() == null) {
                                            replenishAmountErrorStatus = ErrorStatus(isError = true, errorMsg = "Replenish Amount is invalid")
                                            return@BGainOutlineField
                                        }
                                        if (it.toDoubleOrNull() != null){
                                            val doubleReplenishAmount = it.toDouble()
                                            // validate min/max

                                            if (doubleReplenishAmount < 0.0) {
                                                replenishAmountErrorStatus =
                                                    ErrorStatus(isError = true, errorMsg = "Replenish Amount must be greater than 0")
                                                return@BGainOutlineField
                                            }
                                        }
                                    },
                                    errorStatus = replenishAmountErrorStatus
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Row (
                                    horizontalArrangement = Arrangement.End
                                ){
                                    TextButton(onClick = {
                                        showReplenishBudget = false
                                    }) {
                                        Text("Cancel")
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    TextButton(onClick = {
                                        if (replenishAmountErrorStatus.isError) {
                                            return@TextButton
                                        }
                                        coroutineScope.launch {
                                            onReplenishBudget(replenishAmount)
                                            showReplenishBudget = false
                                        }
                                    }) {
                                        Text("Replenish")
                                    }
                                }
                            }
                        }
                    }
                }
            }
            QueryState.ERROR -> {
                Text(text = "Something went wrong!")
            }
        }
        AdMobBanner()
    }
}

@Composable
fun CategoryItem(category: CategoryEntity,
                 onNavigate: (String) -> Unit = {},
                 onDeleteCategory: () -> Unit = {}
                 ) {
    // State to track the expanded state of the menu
    var menuExpanded by remember { mutableStateOf(false) }
    var isShowingDetails by remember { mutableStateOf(false) }

    val rotationAngle by animateFloatAsState(targetValue = if (isShowingDetails) 180f else 0f)

    val floatSpentAmount = category.spentAmount.toFloat()
    val floatAmount = category.amount.toFloat()

    var progress = 0F
    if (floatSpentAmount != 0F && floatAmount != 0F) {
        progress = (floatSpentAmount / floatAmount).coerceAtLeast(
            0F
        ) // Ensure the progress is between 0 and 1
    }
    // Determine the color based on progress
    val progressColor = when {
        progress < 0.5f -> Green700
        progress < 0.8f -> Orange500
        progress <= 1f -> Purple400
        else -> Color.Red
    }

    val cappedProgress = progress.coerceIn(0F, 1F)

    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 2.dp), // Adjust padding if needed
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = category.name,
                    modifier = Modifier
                        .weight(1f),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    "Bal: "
                )
                Text (
                    text = centsToString(category.amount - category.spentAmount),
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = {
                    isShowingDetails = !isShowingDetails
                }){
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Show Details",
                        modifier = Modifier.rotate(rotationAngle)
                    )
                }
                Box {
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
                            onNavigate(Screens.SpendScreen.createRoute(category.id))
                            menuExpanded = false
                        },
                            text = {
                                Text("Spend")
                            })
                        DropdownMenuItem(onClick = {
                            onNavigate(Screens.CategoryDetailsScreen.createRoute(category.id))
                            menuExpanded = false
                        },
                            text = {
                                Text("View Details")
                            })
                        DropdownMenuItem(onClick = {
                            onNavigate(Screens.EditCategoryScreen.createRoute(category.id))
                            menuExpanded = false
                        },
                            text = {
                                Text("Edit")
                            })
                        DropdownMenuItem(onClick = {
                            onDeleteCategory()
                            menuExpanded = false
                        }, text = {
                            Text("Delete")
                        })
                    }
                }

            }
            if (isShowingDetails) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 2.dp), // Adjust padding if needed
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        Text(
                            text = "Budgeted",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = centsToString(category.amount),
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Spent",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = centsToString(category.spentAmount),
                        )
                    }
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        color = progressColor,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinearProgressIndicator(
                    progress = { cappedProgress },
                    color = progressColor,
                    trackColor = Color.LightGray,
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
                if (!isShowingDetails) {
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        color = progressColor,
                    )
                }
            }

        }
    }
}

@Preview
@Composable
fun CategoryItemPreview() {
    BudgetGainTheme {
        Surface {
            CategoryItem(
                category = CategoryEntity(
                    id = 1,
                    budgetId = 1,
                    name = "Groceries",
                    amount = 25000,
                    spentAmount = 15000
                )
            )
        }
    }
}

@Preview
@Composable
fun DarkCategoryItemPreview() {
    BudgetGainTheme(darkTheme = true) {
        Surface {
            CategoryItem(
                category = CategoryEntity(
                    id = 1,
                    budgetId = 1,
                    name = "Groceries",
                    amount = 25000,
                    spentAmount = 15000
                )
            )
        }
    }
}

@Preview
@Composable
fun HomeContentPreview() {
    BudgetGainTheme {
        Surface {
            HomeContent(
                pageState = QueryState.COMPLETE,
                onNavigate = {},
                currentBudget = BudgetEntity(
                    id = 1,
                    yearMonth = "2024-07",
                    initialBalance = 100000,
                    budgetedAmount = 75000,
                    spentAmount = 50000
                ),
                onGetBudgetCategories = { _, _ ->
                    listOf(
                        CategoryEntity(
                            id = 1,
                            budgetId = 1,
                            name = "Groceries",
                            amount = 25000,
                            spentAmount = 15000
                        ),
                        CategoryEntity(
                            id = 2,
                            budgetId = 1,
                            name = "Transport",
                            amount = 10000,
                            spentAmount = 8000
                        )
                    )
                }
            )
        }

    }
}
