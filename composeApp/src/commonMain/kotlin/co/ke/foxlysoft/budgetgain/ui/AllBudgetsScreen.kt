package co.ke.foxlysoft.budgetgain.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import co.ke.foxlysoft.budgetgain.database.BudgetEntity
import co.ke.foxlysoft.budgetgain.navigation.Screens
import co.ke.foxlysoft.budgetgain.utils.centsToString
import co.ke.foxlysoft.budgetgain.utils.dateMillisToString
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(KoinExperimentalAPI::class)
@Composable
fun AllBudgetsScreen(
    allBudgetsScreenViewModel: AllBudgetsScreenViewModel = koinViewModel(),
    onNavigate: (String) -> Unit,
) {
    val allBudgets = allBudgetsScreenViewModel.allBudgets.collectAsState().value

    Box (
        modifier = Modifier
            .fillMaxSize()
    ){
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxSize()
                .background(Color.White)
        ) {
            Text(text = "All Budgets", style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(allBudgets.size) {
                    BudgetItem(allBudgets[it], onActivate = {}, onDelete = {
                        allBudgetsScreenViewModel.deleteBudget(allBudgets[it])
                    })
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
            //        allBudgets.forEach {
            //            BudgetItem(it, {allBudgetsScreenViewModel.deleteBudget(it)})
            //            HorizontalDivider(thickness = 1.dp)
            //        }
        }

        // Floating Action Button (FAB)
        FloatingActionButton(
            onClick = {
                onNavigate(Screens.CreateBudgetScreen.route)
            },
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.BottomEnd),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Budget")
        }
    }
}

@Composable
fun BudgetItem(budget: BudgetEntity, onActivate: () -> Unit, onDelete: () -> Unit) {
    // State to track the expanded state of the menu
    var menuExpanded by remember { mutableStateOf(false) }

    Card {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            Row (
                verticalAlignment = Alignment.CenterVertically,
            ){
                Text(text = budget.name, style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.weight(1f))
                Text(text= "Bal: ${centsToString(budget.budgetedAmount - budget.spentAmount)}",
                    style = MaterialTheme.typography.titleSmall)
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
                        if (!budget.isActive) {
                            DropdownMenuItem(onClick = {
                                onActivate()
                                menuExpanded = false
                            }, text = {
                                Text("Activate")
                            })
                        }
                        DropdownMenuItem(onClick = {
                            onDelete()
                            menuExpanded = false
                        }, text = {
                            Text("Delete")
                        })
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("${dateMillisToString(budget.startDate)} - ${dateMillisToString(budget.endDate)}")
            if (budget.isActive) {
                Box(
                    modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer)
                        .border(1.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small),
                ){
                    Text(text = "Active", modifier = Modifier.padding(4.dp))
                }
            } else {

                Box(
                    modifier = Modifier.background(Color.White)
                        .border(1.dp, Color.Black, MaterialTheme.shapes.small),
                ) {
                    Text(text = "Inactive", modifier = Modifier.padding(4.dp), style = TextStyle(
                        color = Color.Black
                    )
                    )
                }
            }

        }
    }
}