package co.ke.foxlysoft.budgetgain.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.ke.foxlysoft.budgetgain.navigation.Screens
import co.ke.foxlysoft.budgetgain.utils.centsToString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(KoinExperimentalAPI::class)
@Composable
fun HomeScreen(
    homeScreenViewModel: HomeScreenViewModel = koinViewModel(),
    onNavigate: (String) -> Unit,
) {
    val firstTime = homeScreenViewModel.firstTime.collectAsState().value
    val currentBudget = homeScreenViewModel.currentBudget.collectAsState().value

    val budgetCategories by remember(currentBudget.id) {
        homeScreenViewModel.getBudgetCategories(currentBudget.id)
    }.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        if (currentBudget.name.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Column(){
                    Text(text = "Welcome! to Budget Gain.")
                    Text(text = "To get started, create a budget.")
                    Button(onClick = {
                        onNavigate(Screens.CreateBudgetScreen.route)
                    }) {
                        Text(text = "Create Budget")
                    }
                }

            }

        } else {
            Box(
                modifier = Modifier.padding(16.dp),
            ){
                Column(){
                    Text(
                        text = currentBudget.name,
                        fontSize = 18.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Text(text = "Initial Balance: ${centsToString(currentBudget.initialBalance)}")
                    Row(
                    ){
                        Text(text = "Budgeted Amount: ${currentBudget.budgetedAmount}")
                        Spacer(modifier = Modifier.weight(1f))
                        Text(text = "Spent Amount: ${currentBudget.spentAmount}")
                    }
                    Column(
                        modifier = Modifier.fillMaxWidth()
                            .background(Color.White)
                            .shadow(1.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "Expense Categories")
                                IconButton(onClick = {
                                    onNavigate(Screens.AddCategoryScreen.createRoute(currentBudget.id))
                                }) {
                                    Icon(
                                        imageVector = androidx.compose.material.icons.Icons.Default.Add,
                                        contentDescription = "Add Category"
                                    )
                                }
                            }
                        }

                        Column {
                            budgetCategories.forEach { category ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp), // Adjust padding if needed
                                    verticalAlignment = Alignment.CenterVertically
                                ){
                                    Text(
                                        text = category.name,
                                        modifier = Modifier
                                            .weight(1f)
                                    )
                                    IconButton(onClick = {
                                    }) {
                                        Icon(
                                            imageVector = androidx.compose.material.icons.Icons.Default.Add,
                                            contentDescription = "Spend"
                                        )
                                    }
                                }
                            }
                        }

                        Text(text = "10/10")
                    }
                }


            }
        }
    }
}