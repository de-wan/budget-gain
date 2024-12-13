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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.ke.foxlysoft.budgetgain.database.CategoryEntity
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
    val coroutineScope = rememberCoroutineScope()

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
                        Text(text = "Budgeted Amount: ${centsToString(currentBudget.budgetedAmount)}")
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
                                Text(text = "Expense Categories", style = TextStyle(fontWeight = FontWeight.Bold))
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
                                CategoryItem(category, onNavigate = onNavigate, onDeleteCategory = {
                                    coroutineScope.launch {
                                        homeScreenViewModel.deleteCategory(category)
                                    }
                                })
                            }
                        }

                        Text(text = "10/10")
                    }
                }




            }
        }
    }
}

@Composable
fun CategoryItem(category: CategoryEntity,
                 onNavigate: (String) -> Unit = {},
                 onDeleteCategory: () -> Unit = {}
                 ) {
    // State to track the expanded state of the menu
    var menuExpanded by remember { mutableStateOf(false) }

    val progress = (category.spentAmount / category.amount).coerceIn(0L, 1L) // Ensure the progress is between 0 and 1

    // Determine the color based on progress
    val progressColor = when {
        progress < 0.5f -> Color.Green
        progress < 0.8f -> Color.Yellow
        else -> Color.Red
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 2.dp), // Adjust padding if needed
            verticalAlignment = Alignment.CenterVertically
        ){
            Text(
                text = category.name,
                modifier = Modifier
                    .weight(1f),
                style = MaterialTheme.typography.bodyLarge
            )
            Box{
                IconButton(onClick = {
                    menuExpanded = true
                }) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.MoreVert,
                        contentDescription = "Menu"
                    )
                }

                // Dropdown menu
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(onClick = {
                    onNavigate(Screens.SpendScreen.createRoute(category.id))
                        menuExpanded = false
                    },
                        text = {
                            Text("Spend")
                        })
                    DropdownMenuItem(onClick = {
//                onNavigate(Screens.CategoryDetailsScreen.createRoute(category.id))
                        menuExpanded = false
                    },
                        text = {
                            Text("View Details")
                        })
                    DropdownMenuItem(onClick = {
//                onNavigate(Screens.CategoryEditScreen.createRoute(category.id))
                        menuExpanded = false
                    },
                        text = {
                            Text("Edit")
                        })
                    DropdownMenuItem(onClick = {
                        onDeleteCategory()
                        menuExpanded = false
                    }, text = {
                        Text("Delete")
                    })
                }
            }

        }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 2.dp), // Adjust padding if needed
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    Text(
                        text = "Budgeted",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = centsToString(category.amount),
                    )
                }
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Spent",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = centsToString(category.spentAmount),
                    )
                }
                Text(
                    text = "${progress * 100}%",
                    color = progressColor,
                )
            }
            LinearProgressIndicator(
                progress = { progress.toFloat() },
                color = progressColor,
                trackColor = Color.LightGray,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
        }
    }