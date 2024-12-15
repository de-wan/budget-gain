package co.ke.foxlysoft.budgetgain.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import co.ke.foxlysoft.budgetgain.database.BudgetEntity
import co.ke.foxlysoft.budgetgain.navigation.Screens
import co.ke.foxlysoft.budgetgain.ui.components.BGainOutlineField
import co.ke.foxlysoft.budgetgain.utils.ErrorStatus
import co.ke.foxlysoft.budgetgain.utils.amountToCents
import co.touchlab.kermit.Logger
import kotlinx.datetime.LocalDate
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(ExperimentalMaterial3Api::class, KoinExperimentalAPI::class)
@Composable
fun CreateBudgetScreen(
    CreateBudgetScreenViewModel: CreateBudgetScreenViewModel = koinViewModel(),
    onNavigate: (String) -> Unit
) {
    var budgetName by remember { mutableStateOf(TextFieldValue("")) }
    var budgetNameErrorStatus by remember { mutableStateOf(ErrorStatus(isError = false))}
    var budgetAmount by remember { mutableStateOf(TextFieldValue("")) }
    var budgetAmountErrorStatus by remember { mutableStateOf(ErrorStatus(isError = false))}
    var startDate by remember { mutableStateOf(0L) }
    var startDateErrorStatus by remember { mutableStateOf(ErrorStatus(isError = false))}
    var endDate by remember { mutableStateOf(0L) }
    var endDateErrorStatus by remember { mutableStateOf(ErrorStatus(isError = false))}
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var submitAttempted by remember { mutableStateOf(false) }

    fun clearErrorStatus() {
        budgetNameErrorStatus = ErrorStatus(isError = false)
        budgetAmountErrorStatus = ErrorStatus(isError = false)
        endDateErrorStatus = ErrorStatus(isError = false)
    }

    fun isFormValid(): Boolean {
        var isValid = true
        if (budgetName.text.isEmpty()) {
            budgetNameErrorStatus = ErrorStatus(isError = true, errorMsg = "Budget Name is required")
            isValid = false
        }
        if (budgetAmount.text.isEmpty()) {
            budgetAmountErrorStatus = ErrorStatus(isError = true, errorMsg = "Budget Amount is required")
            isValid = false
        } else if(budgetAmount.text.toFloatOrNull() == null) {
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
                    textFieldInput = budgetName,
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
                    labelStr = "Budget Amount",
                    textFieldInput = budgetAmount,
                    errorStatus = budgetAmountErrorStatus,
                    onValueChange = { budgetAmount = it },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    validator = {
                        if (it.isEmpty()){
                            budgetAmountErrorStatus = ErrorStatus(isError = true, errorMsg = "Budget Amount is required")
                            return@BGainOutlineField
                        }
                        if (it.toFloatOrNull() == null) {
                            budgetAmountErrorStatus = ErrorStatus(isError = true, errorMsg = "Budget Amount is invalid")
                            return@BGainOutlineField
                        }
                        budgetAmountErrorStatus = ErrorStatus(isError = false)
                    },
                    submitAttempted = submitAttempted
                )
                BGainOutlineField(
                    onDateChange = { startDate = it },
                    labelStr = "Start Date",
                    errorStatus = startDateErrorStatus,
                    isDatePicker = true,
                    modifier = Modifier
                        .fillMaxWidth(),
                    submitAttempted = submitAttempted
                )
                BGainOutlineField(
                    onDateChange = { endDate = it },
                    labelStr = "End Date",
                    errorStatus = endDateErrorStatus,
                    isDatePicker = true,
                    modifier = Modifier
                        .fillMaxWidth(),
                    submitAttempted = submitAttempted
                )
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

                        var budget = BudgetEntity(
                            name = budgetName.text,
                            initialBalance = amountToCents(budgetAmount.text),
                            startDate = startDate,
                            endDate = endDate,
                            isActive = false,
                            budgetedAmount = 0L,
                            spentAmount = 0L
                        )
                        CreateBudgetScreenViewModel.createBudget(budget)
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


