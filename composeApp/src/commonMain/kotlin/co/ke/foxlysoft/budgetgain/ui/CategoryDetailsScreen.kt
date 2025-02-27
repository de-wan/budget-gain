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
import co.ke.foxlysoft.budgetgain.ui.components.BGPaginatedList
import co.ke.foxlysoft.budgetgain.utils.PaginationState
import co.ke.foxlysoft.budgetgain.utils.centsToString
import kotlinx.coroutines.launch
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
    val coroutineScope = rememberCoroutineScope()

    var refreshAllPages by remember { mutableStateOf({}) }

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
            BGPaginatedList(
                onGetKey = { it.id },
                onGetItem = {
                    TransactionItem(categoryDetailsScreenViewModel, it, onDelete = {
                        // TODO: add a confirmation dialog
                        onOpenConfirmSnackbar(
                            "Are you sure you want to delete?",
                            "Confirm",
                            {
                                // Perform the delete action
                                categoryDetailsScreenViewModel.deleteTransaction(it, refreshAllPages)
                            }
                        )

                    })
                },
                onGetItems = { limit, offset ->
                    categoryDetailsScreenViewModel.getCategoryTransactions(limit, offset)
                },
                onRefreshAllPagesReady = { refreshAllPages = it },
            )
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