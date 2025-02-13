package co.ke.foxlysoft.budgetgain.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.zIndex
import budgetgain.composeapp.generated.resources.Res
import budgetgain.composeapp.generated.resources.calculator_variant_outline
import co.ke.foxlysoft.budgetgain.database.BudgetEntity
import co.ke.foxlysoft.budgetgain.navigation.Screens
import co.ke.foxlysoft.budgetgain.ui.components.BGainOutlineField
import co.ke.foxlysoft.budgetgain.ui.components.CalculatorDialog
import co.ke.foxlysoft.budgetgain.utils.ErrorStatus
import co.ke.foxlysoft.budgetgain.utils.amountToCents
import co.ke.foxlysoft.budgetgain.utils.isValidAmount
import co.touchlab.kermit.Logger
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(KoinExperimentalAPI::class)
@Composable
fun CreateBudgetScreen(
    createBudgetScreenViewModel: CreateBudgetScreenViewModel = koinViewModel(),
    onNavigate: (String) -> Unit
) {
    var isCalculatorOpen by remember {mutableStateOf(false)}

    val selectableBudgets by createBudgetScreenViewModel.selectableBudgets.collectAsState()

    var budgetName by remember { mutableStateOf("") }
    var budgetNameErrorStatus by remember { mutableStateOf(ErrorStatus(isError = false))}
    var budgetAmount by remember { mutableStateOf("") }
    var budgetAmountErrorStatus by remember { mutableStateOf(ErrorStatus(isError = false))}
    var startDate by remember { mutableStateOf(0L) }
    var startDateErrorStatus by remember { mutableStateOf(ErrorStatus(isError = false))}
    var startDateString by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf(0L) }
    var endDateErrorStatus by remember { mutableStateOf(ErrorStatus(isError = false))}
    var endDateString by remember { mutableStateOf("") }
    var copyCategoriesFrom by remember { mutableStateOf("") }
    var copyCategoriesFromErrorStatus by remember { mutableStateOf(ErrorStatus(isError = false))}
    var copyCategoriesFromAutoCompleteExpanded by remember { mutableStateOf(false) }
//    var errorMessage by remember { mutableStateOf<String?>(null) }
    var submitAttempted by remember { mutableStateOf(false) }

    fun clearErrorStatus() {
        budgetNameErrorStatus = ErrorStatus(isError = false)
        budgetAmountErrorStatus = ErrorStatus(isError = false)
        endDateErrorStatus = ErrorStatus(isError = false)
    }

    fun isFormValid(): Boolean {
        var isValid = true
        if (budgetName.isEmpty()) {
            budgetNameErrorStatus = ErrorStatus(isError = true, errorMsg = "Budget Name is required")
            isValid = false
        }
        if (budgetAmount.isEmpty()) {
            budgetAmountErrorStatus = ErrorStatus(isError = true, errorMsg = "Budget Amount is required")
            isValid = false
        } else if(budgetAmount.toFloatOrNull() == null) {
            budgetAmountErrorStatus =
                ErrorStatus(isError = true, errorMsg = "Budget Amount is invalid")
            isValid = false
        }
        if (startDate == 0L) {
            startDateErrorStatus = ErrorStatus(isError = true, errorMsg = "Start Date is required")
            isValid = false
        }
        if (endDate == 0L) {
            endDateErrorStatus = ErrorStatus(isError = true, errorMsg = "End Date is required")
            isValid = false
        }
        // end date must be greater than start date
        if (startDate > 0L && endDate > 0L && startDate > endDate) {
                endDateErrorStatus = ErrorStatus(isError = true, errorMsg = "End Date must be greater than Start Date")
                isValid = false
        }
        return isValid
    }

    if (isCalculatorOpen) {
        CalculatorDialog(
            onDismissRequest = { isCalculatorOpen = false },
            onApply = {
                if (isValidAmount(it)) {
                    budgetAmount = it
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Text(modifier = Modifier.padding(16.dp), text = "Create Budget Screen")
        Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column {
                BGainOutlineField(
                    modifier = Modifier
                        .fillMaxWidth(),
                    labelStr = "Budget Name",
                    Value = budgetName,
                    errorStatus = budgetNameErrorStatus,
                    onValueChange = { budgetName = it },
                    validator = {
                        if (it.isEmpty()){
                            budgetNameErrorStatus = ErrorStatus(isError = true, errorMsg = "Budget Name is required")
                            return@BGainOutlineField
                        }
                        budgetNameErrorStatus = ErrorStatus(isError = false)
                    },
                    submitAttempted = submitAttempted
                )
                BGainOutlineField(
                    modifier = Modifier
                        .fillMaxWidth(),
                    labelStr = "Initial Amount",
                    Value = budgetAmount,
                    errorStatus = budgetAmountErrorStatus,
                    onValueChange = { budgetAmount = it },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    validator = {
                        if (it.isEmpty()){
                            budgetAmountErrorStatus = ErrorStatus(isError = true, errorMsg = "Initial Amount is required")
                            return@BGainOutlineField
                        }
                        if (it.toFloatOrNull() == null) {
                            budgetAmountErrorStatus = ErrorStatus(isError = true, errorMsg = "Initial Amount is invalid")
                            return@BGainOutlineField
                        }
                        budgetAmountErrorStatus = ErrorStatus(isError = false)
                    },
                    submitAttempted = submitAttempted,
                    trailingIcon = {
                        IconButton(onClick = {isCalculatorOpen = true}){
                            Icon(
                                painter = painterResource(Res.drawable.calculator_variant_outline),
                                contentDescription = "Open Calculator"
                            )
                        }
                    }
                )
                BGainOutlineField(
                    Value = startDateString,
                    onDateChange = { millis, dateStr ->
                        startDate = millis
                        startDateString = dateStr },
                    labelStr = "Start Date",
                    errorStatus = startDateErrorStatus,
                    isDatePicker = true,
                    modifier = Modifier
                        .fillMaxWidth(),
                    submitAttempted = submitAttempted
                )
                BGainOutlineField(
                    Value = endDateString,
                    onDateChange = { millis, dateStr ->
                        endDate =  millis
                        endDateString = dateStr
                                   },
                    labelStr = "End Date",
                    errorStatus = endDateErrorStatus,
                    isDatePicker = true,
                    modifier = Modifier
                        .fillMaxWidth(),
                    submitAttempted = submitAttempted
                )
                BGainOutlineField(
                    modifier = Modifier
                        .fillMaxWidth(),
                    labelStr = "Copy categories from",
                    Value = copyCategoriesFrom,
                    errorStatus = copyCategoriesFromErrorStatus,
                    onValueChange = {
                        println("Budget to copy from changed to $it")
                        createBudgetScreenViewModel.updateBudgetSearchQuery(it)
                        copyCategoriesFromAutoCompleteExpanded = true

                        copyCategoriesFrom = it
                    },
                    validator = {
                        // TODO: validate if in list
                        copyCategoriesFromErrorStatus = ErrorStatus(isError = false)
                    },
                    submitAttempted = submitAttempted
                )
                // Dropdown menu
                Box {
                    if (copyCategoriesFromAutoCompleteExpanded && selectableBudgets.isNotEmpty()) {
                        Popup(
                            onDismissRequest = { copyCategoriesFromAutoCompleteExpanded = false },
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 32.dp)
                                    .zIndex(1f),
                            ) {
                                LazyColumn {
                                    items(items = selectableBudgets) { budget ->
                                        TextButton(
                                            modifier = Modifier
                                                .fillMaxWidth(),
                                            onClick = {
                                                copyCategoriesFrom = budget.name
                                                copyCategoriesFromAutoCompleteExpanded = false
                                            },
                                        ){
                                            Text(text = budget.name)
                                        }
                                    }

                                }
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(onClick = {
                        submitAttempted = true
                        clearErrorStatus()
                        val isFormValid = isFormValid()
                        Logger.d("isFormValid: $isFormValid")
                        if (!isFormValid) {
                            return@Button
                        }

                        val budget = BudgetEntity(
                            name = budgetName,
                            initialBalance = amountToCents(budgetAmount),
                            startDate = startDate,
                            endDate = endDate,
                            isActive = false,
                            budgetedAmount = 0L,
                            spentAmount = 0L
                        )
                        createBudgetScreenViewModel.createBudget(budget, copyCategoriesFrom)
                        onNavigate(Screens.Home.route)
                    }) {
                        Text(text = "Create Budget")
                    }
                    FilledTonalButton(onClick = {
                    }) {
                        Text(text = "Cancel")
                    }
                }
            }

        }
    }
}


