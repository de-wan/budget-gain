package co.ke.foxlysoft.budgetgain.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.ke.foxlysoft.budgetgain.database.TransactionEntity
import co.ke.foxlysoft.budgetgain.ui.components.BGPaginatedList
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
            BGPaginatedList(
                onGetKey = { it.id },
                onGetItem = { transaction ->
                    TransactionItem(transaction)
                },
                onGetItems = { limit, offset ->
                    merchantTransactionsViewModel.getMerchantTransactions(limit, offset)
                }
            )
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