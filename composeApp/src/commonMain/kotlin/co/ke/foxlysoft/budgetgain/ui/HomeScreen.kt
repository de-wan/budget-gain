package co.ke.foxlysoft.budgetgain.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.ke.foxlysoft.budgetgain.database.CategoryEntity
import co.ke.foxlysoft.budgetgain.database.MpesaSmsEntity
import co.ke.foxlysoft.budgetgain.navigation.Screens
import co.ke.foxlysoft.budgetgain.shared.PermissionLaucher
import co.ke.foxlysoft.budgetgain.shared.SmsReader
import co.ke.foxlysoft.budgetgain.ui.Theme.Green700
import co.ke.foxlysoft.budgetgain.ui.Theme.Orange500
import co.ke.foxlysoft.budgetgain.ui.components.BGPaginatedList
import co.ke.foxlysoft.budgetgain.utils.MpesaSmsTypes
import co.ke.foxlysoft.budgetgain.utils.QueryState
import co.ke.foxlysoft.budgetgain.utils.centsToString
import co.ke.foxlysoft.budgetgain.utils.smsParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(KoinExperimentalAPI::class)
@Composable
fun HomeScreen(
     homeScreenViewModel: HomeScreenViewModel = koinViewModel(),
    onNavigate: (String) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()

    // PENDING, NO_CURRENT_BUDGET, COMPLETE, ERROR

    val currentBudget = homeScreenViewModel.currentBudget.collectAsState().value
    val pageState = homeScreenViewModel.pageState.collectAsStateWithLifecycle()
    var isPermissionGranted by remember {
        mutableStateOf(false)
    }
    PermissionLaucher(
        onPermissionGranted = {
            isPermissionGranted = true
            println("Permission granted")
        },
        onPermissionDenied = {
            isPermissionGranted = false
        }
    )


    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        when (pageState.value) {
            QueryState.LOADING -> {
                CircularProgressIndicator()
            }
            QueryState.NO_RESULTS -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column{
                        Text(text = "Welcome! to Budget Gain.")
                        Text(text = "To get started, create a budget.")
                        Button(onClick = {
                            onNavigate(Screens.CreateBudgetScreen.route)
                        }) {
                            Text(text = "Create Budget")
                        }
                    }

                }
            }
            QueryState.COMPLETE -> {
                if (isPermissionGranted) {
                    println("fetching sms")
                    coroutineScope.launch {
                        withContext(Dispatchers.Default) {
                            // TODO: get date of last saved sms in budget time range

                            val rawMpesaSms = SmsReader().getMpesaSms(currentBudget.startDate, currentBudget.endDate)

                            for (rawSms in rawMpesaSms) {
                                val sms = smsParser(rawSms)
                                if (sms.smsType == MpesaSmsTypes.UNKNOWN) {
                                    println("Unknown sms: $rawSms")
                                    continue
                                }

                                val mpesaSmsEntity = MpesaSmsEntity(
                                    smsType = sms.smsType,
                                    ref = sms.ref,
                                    amount = sms.amount,
                                    dateTime = sms.dateTime,
                                    subjectPrimaryIdentifierType = sms.subjectPrimaryIdentifierType,
                                    subjectPrimaryIdentifier = sms.subjectPrimaryIdentifier,
                                    subjectSecondaryIdentifierType = sms.subjectSecondaryIdentifierType,
                                    subjectSecondaryIdentifier = sms.subjectSecondaryIdentifier,
                                    cost = sms.cost,
                                    balance = sms.balance,
                                )

                                homeScreenViewModel.upsertMpesaSms(mpesaSmsEntity)
                            }
                        }
                    }

                }

                Box(
                    modifier = Modifier.padding(16.dp),
                ){
                    Column{
                        Text(
                            text = currentBudget.name,
                            fontSize = 18.sp,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )

                        Text(text = "Initial Balance: ${centsToString(currentBudget.initialBalance)}")
                        Text(text = "Current Balance: ${centsToString(currentBudget.initialBalance - currentBudget.spentAmount)}")

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ){
                            Column {
                                Text(text = "UnBudgeted:")
                                Text(text = centsToString(currentBudget.initialBalance - currentBudget.budgetedAmount))
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            VerticalDivider(
                                modifier = Modifier.height(30.dp)
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Column {
                                Text(text = "Budgeted:")
                                Text(text = centsToString(currentBudget.budgetedAmount))
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            VerticalDivider(
                                modifier = Modifier.height(30.dp)
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Column {
                                Text(text = "Spent:")
                                Text(text = centsToString(currentBudget.spentAmount))
                            }
                        }

                        HorizontalDivider()

                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box {
                                Row(
                                    modifier = Modifier.fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Expense Categories",
                                        style = TextStyle(fontWeight = FontWeight.Bold)
                                    )
                                    IconButton(onClick = {
                                        onNavigate(
                                            Screens.AddCategoryScreen.createRoute(
                                                currentBudget.id
                                            )
                                        )
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Add Category"
                                        )
                                    }
                                }
                            }

                            Column {
                                BGPaginatedList(
                                    onGetKey = { it.id },
                                    onGetItem = {
                                        CategoryItem(it, onNavigate = onNavigate, onDeleteCategory = {
                                            coroutineScope.launch {
                                                homeScreenViewModel.deleteCategory(it)
                                            }
                                        })
                                    },
                                    onGetItems = { limit, offset ->
                                        homeScreenViewModel.getBudgetCategories(limit, offset)
                                    }
                                )
                            }
                        }
                    }
                }
            }
            QueryState.ERROR -> {
                Text(text = "Something went wrong!")
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

    val floatSpentAmount = category.spentAmount.toFloat()
    val floatAmount = category.amount.toFloat()

    var progress = 0F
    if (floatSpentAmount != 0F && floatAmount != 0F) {
        progress = (floatSpentAmount / floatAmount).coerceIn(
            0F,
            1F
        ) // Ensure the progress is between 0 and 1
    }
    // Determine the color based on progress
    val progressColor = when {
        progress < 0.5f -> Green700
        progress < 0.8f -> Orange500
        else -> Color.Red
    }

    Card {
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
            ) {
                Text(
                    text = category.name,
                    modifier = Modifier
                        .weight(1f),
                    style = MaterialTheme.typography.bodyLarge
                )
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
                            onNavigate(Screens.SpendScreen.createRoute(category.id))
                            menuExpanded = false
                        },
                            text = {
                                Text("Spend")
                            })
                        DropdownMenuItem(onClick = {
                            onNavigate(Screens.CategoryDetailsScreen.createRoute(category.id))
                            menuExpanded = false
                        },
                            text = {
                                Text("View Details")
                            })
                        DropdownMenuItem(onClick = {
                            onNavigate(Screens.EditCategoryScreen.createRoute(category.id))
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
                    text = "${(progress * 100).toInt()}%",
                    color = progressColor,
                )
            }
            LinearProgressIndicator(
                progress = { progress },
                color = progressColor,
                trackColor = Color.LightGray,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
        }
    }
    }