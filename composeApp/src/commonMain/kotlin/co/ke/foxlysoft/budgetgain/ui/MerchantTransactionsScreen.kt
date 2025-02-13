package co.ke.foxlysoft.budgetgain.ui

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.ke.foxlysoft.budgetgain.database.TransactionEntity
import co.ke.foxlysoft.budgetgain.utils.PaginationState
import co.ke.foxlysoft.budgetgain.utils.centsToString
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.parameter.parametersOf

@OptIn(KoinExperimentalAPI::class)
@Composable
fun MerchantTransactionsScreen(
    merchantId: Long,
    merchantTransactionsViewModel: MerchantTransactionsScreenViewModel = koinViewModel(parameters = { parametersOf(merchantId) }),
) {
    val merchant = merchantTransactionsViewModel.currentMerchant.collectAsState().value

    val lazyColumnListState = rememberLazyListState()

    val transactionsList by merchantTransactionsViewModel.transactionsList.collectAsStateWithLifecycle()
    val pagingState = merchantTransactionsViewModel.pagingState.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = Unit) {
        merchantTransactionsViewModel.clearPaging()
        merchantTransactionsViewModel.getMerchantTransactions(merchantId)
    }

    val shouldPaginate = remember {
        derivedStateOf {
            merchantTransactionsViewModel.canPaginate && (
                    lazyColumnListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
                        ?: -5
                    ) >= (lazyColumnListState.layoutInfo.totalItemsCount - 3)
        }
    }

    LaunchedEffect(key1 = shouldPaginate.value) {
        if (shouldPaginate.value && pagingState.value == PaginationState.REQUEST_INACTIVE) {
            merchantTransactionsViewModel.getMerchantTransactions(merchantId)
        }
    }

    Column (modifier = Modifier.fillMaxSize()
            .padding(16.dp)
    ){
        Text(text= "'${merchant.merchantName}' Transactions", style = MaterialTheme.typography.headlineMedium)
        Text(text = "Merchant Transactions")
        Spacer(modifier = Modifier.height(8.dp))

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
                    TransactionItem(transactionsList[index])
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
    transaction: TransactionEntity,
) {
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
            Column(modifier = Modifier.weight(6f)) {
                Text(text = "#${transaction.ref}", style = TextStyle(fontSize = 12.sp))
                Text(text= description, style = TextStyle(fontSize = 14.sp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column (modifier = Modifier.weight(3f)) {
                Text(text = transaction.timestamp, style = TextStyle(fontSize = 12.sp))
                Text(text = "Ksh${centsToString(transaction.amount)}", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}