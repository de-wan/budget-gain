package co.ke.foxlysoft.budgetgain.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.ke.foxlysoft.budgetgain.database.AccountEntity
import co.ke.foxlysoft.budgetgain.database.TransactionEntity
import co.ke.foxlysoft.budgetgain.navigation.Screens
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

    val categoryTransactions by remember(category.id) {
        categoryDetailsScreenViewModel.getCategoryTransactions(category.id)
    }.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize()
            .padding(16.dp)
    ){
        Text(text= category.name, style = MaterialTheme.typography.headlineLarge)
        Text(text = "Remaining: Ksh${centsToString(category.amount - category.spentAmount)}")
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider()
        Text(text = "Transactions")

        categoryTransactions.forEach { transaction ->
            TransactionItem(categoryDetailsScreenViewModel, transaction, onDelete = {
                // TODO: add a confirmation dialog
                categoryDetailsScreenViewModel.deleteTransaction(transaction)
            })
            Spacer(modifier = Modifier.height(4.dp))
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

    LaunchedEffect(Unit) {
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
            Column {
                Text(text = "#${transaction.ref}", style = TextStyle(fontSize = 12.sp))
                Text(text= merchantAccount.merchantName)
                Text(text= transaction.description, style = TextStyle(fontSize = 14.sp))
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