package co.ke.foxlysoft.budgetgain.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.ke.foxlysoft.budgetgain.database.AccountEntity
import co.ke.foxlysoft.budgetgain.database.TransactionEntity
import co.ke.foxlysoft.budgetgain.navigation.Screens
import co.ke.foxlysoft.budgetgain.utils.PaginationState
import co.ke.foxlysoft.budgetgain.utils.centsToString
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.parameter.parametersOf

@OptIn(KoinExperimentalAPI::class)
@Composable
fun CategoryDetailsScreen(
    categoryId: Long,
    categoryDetailsScreenViewModel: CategoryDetailsScreenViewModel = koinViewModel(parameters = { parametersOf(categoryId) }),
    onNavigateBack: () -> Unit
){
    val category = categoryDetailsScreenViewModel.currentCategory.collectAsState().value

    val lazyColumnListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()


    val transactionsList by categoryDetailsScreenViewModel.transactionsList.collectAsStateWithLifecycle()
    val pagingState = categoryDetailsScreenViewModel.pagingState.collectAsStateWithLifecycle()

    val testList = listOf("Item 1", "Item 2", "Item 3")

    LaunchedEffect(key1 = Unit) {
        categoryDetailsScreenViewModel.clearPaging()
        categoryDetailsScreenViewModel.getCategoryTransactions(categoryId)
    }

    val shouldPaginate = remember {
        derivedStateOf {
            categoryDetailsScreenViewModel.canPaginate && (
                    lazyColumnListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
                        ?: -5
                    ) >= (lazyColumnListState.layoutInfo.totalItemsCount - 3)
        }
    }

    LaunchedEffect(key1 = shouldPaginate.value) {
        if (shouldPaginate.value && pagingState.value == PaginationState.REQUEST_INACTIVE) {
            categoryDetailsScreenViewModel.getCategoryTransactions(categoryId)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
            .padding(16.dp)
    ){
        Text(text= category.name, style = MaterialTheme.typography.headlineLarge)
        Text(text = "Remaining: Ksh${centsToString(category.amount - category.spentAmount)}")
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider()
        Text(text = "Transactions")

        ElevatedCard (
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            LazyColumn(
                state = lazyColumnListState,
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                items(
                    transactionsList.size,
                    key = { transactionsList[it].id },
                ) { index ->
                    TransactionItem(categoryDetailsScreenViewModel, transactionsList[index], onDelete = {
                        // TODO: add a confirmation dialog
                        categoryDetailsScreenViewModel.deleteTransaction(transactionsList[index])
                    })
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }

        when (pagingState.value) {
            PaginationState.LOADING -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            PaginationState.REQUEST_INACTIVE -> {
//                Text(text = "Request Inactive")
            }
            PaginationState.PAGINATING -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            PaginationState.ERROR -> {
//                Text(text = "Error")
            }
            PaginationState.PAGINATION_EXHAUST -> {
//                Text(text = "Pagination Exhaust")
            }
            PaginationState.EMPTY -> {
//                Text(text = "Empty")
            }
        }
    }
}

@Composable
fun TransactionItem(
    categoryDetailsScreenViewModel: CategoryDetailsScreenViewModel,
    transaction: TransactionEntity,
    onDelete: () -> Unit
) {
    // State to track the expanded state of the menu
    var menuExpanded by remember { mutableStateOf(false) }

    var merchantAccount by remember { mutableStateOf(AccountEntity()) }

    LaunchedEffect(key1 = Unit) {
        categoryDetailsScreenViewModel.getMerchantAccount(transaction) {
            merchantAccount = it
        }
    }


    Card{
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            var description = transaction.description
            if (transaction.description.length > 10) {
                description = description.substring(0,10)+"..."
            }
            Column {
                Text(text = "#${transaction.ref}", style = TextStyle(fontSize = 12.sp))
                Text(text= merchantAccount.merchantName)
                Text(text= description, style = TextStyle(fontSize = 14.sp))
            }
            Spacer(modifier = Modifier.weight(1f))
            Column {
                Text(text = transaction.timestamp, style = TextStyle(fontSize = 12.sp))
                Text(text = "Ksh${centsToString(transaction.amount)}", style = MaterialTheme.typography.bodyLarge)
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