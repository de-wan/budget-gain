package co.ke.foxlysoft.budgetgain.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
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
import androidx.compose.ui.unit.dp
import co.ke.foxlysoft.budgetgain.database.AccountEntity
import co.ke.foxlysoft.budgetgain.utils.centsToString
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(KoinExperimentalAPI::class)
@Composable
fun MerchantsScreen(
    merchantsScreenViewModel: MerchantsScreenViewModel = koinViewModel(),
    onNavigate: (String) -> Unit,
) {
    val merchantAccounts = merchantsScreenViewModel.merchantAccounts.collectAsState().value

    Column(
        modifier = Modifier.fillMaxSize()
            .padding(16.dp)
    ){
        Text(text= "Merchant Accounts", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        merchantAccounts.forEach { account ->
            MerchantItem(merchantsScreenViewModel, account)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }

}

@Composable
fun MerchantItem(merchantsScreenViewModel : MerchantsScreenViewModel, merchantAccount: AccountEntity) {
    val totalSpent = centsToString(merchantAccount.balance)

    var budgetSpend by remember{mutableStateOf("")}
    LaunchedEffect(key1 = Unit) {
        budgetSpend = centsToString(merchantsScreenViewModel.getMerchantAccountBudgetSpend(merchantAccount.id))
    }

    Card {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ){
            Column {
                Text(text = merchantAccount.merchantName, style = MaterialTheme.typography.bodyLarge)
                Row{
                    Text(text= "total spent: $totalSpent Ksh", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.weight(1f))
                    Text(text= "budget spent: $budgetSpend Ksh", style = MaterialTheme.typography.bodySmall)
                }

            }

        }
    }
}