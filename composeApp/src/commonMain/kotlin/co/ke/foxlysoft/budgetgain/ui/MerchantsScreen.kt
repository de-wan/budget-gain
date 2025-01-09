package co.ke.foxlysoft.budgetgain.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
        Text(text= "Merchant Accounts")
        merchantAccounts.forEach { account ->
            Text(text = account.id.toString() + account.merchantName + " '"+account.name+"'")
        }
    }

}