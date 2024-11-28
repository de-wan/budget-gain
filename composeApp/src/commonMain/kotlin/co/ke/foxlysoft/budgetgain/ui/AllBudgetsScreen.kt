package co.ke.foxlysoft.budgetgain.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(KoinExperimentalAPI::class)
@Composable
fun AllBudgetsScreen(
    allBudgetsScreenViewModel: AllBudgetsScreenViewModel = koinViewModel(),
    onNavigate: (String) -> Unit,
) {
    val allBudgets = allBudgetsScreenViewModel.allBudgets.collectAsState().value

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
            .background(Color.White)
    ) {
        Row(){
            Button(onClick = {

            }) {
                Text(text = "Create Budget")
            }
        }
        Text(text = "All Budgets")
        allBudgets.forEach {
            Text(text = it.name)
            Text(text = it.budgetedAmount.toString())
            Text(text = it.startDate)
            Text(text = it.endDate)
            Button(onClick = {
                allBudgetsScreenViewModel.deleteBudget(it)
            }){
                Text("Delete")
            }
        }
    }
}