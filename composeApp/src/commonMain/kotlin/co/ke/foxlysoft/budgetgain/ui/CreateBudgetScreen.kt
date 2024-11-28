package co.ke.foxlysoft.budgetgain.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import co.ke.foxlysoft.budgetgain.database.BudgetEntity
import co.ke.foxlysoft.budgetgain.navigation.Screens
import co.ke.foxlysoft.budgetgain.ui.components.BGainOutlineField
import co.ke.foxlysoft.budgetgain.utils.ErrorStatus
import co.touchlab.kermit.Logger
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
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
    var startDate by remember { mutableStateOf("Select Start Date") }
    var startDateErrorStatus by remember { mutableStateOf(ErrorStatus(isError = false))}
    var endDate by remember { mutableStateOf("Select End Date") }
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
        if (startDate.isEmpty()) {
            startDateErrorStatus = ErrorStatus(isError = true, errorMsg = "Start Date is required")
            isValid = false
        }
        if (endDate.isEmpty()) {
            endDateErrorStatus = ErrorStatus(isError = true, errorMsg = "End Date is required")
            isValid = false
        }
        // end date must be greater than start date
        if (startDate.isNotEmpty() && endDate.isNotEmpty()) {
            val start = LocalDate.parse(startDate)
            val end = LocalDate.parse(endDate)
            if (start > end) {
                endDateErrorStatus = ErrorStatus(isError = true, errorMsg = "End Date must be greater than Start Date")
                isValid = false
            }
        }
        return isValid
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
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

                        val budget = BudgetEntity(
                            name = budgetName.text,
                            initialBalance = budgetAmount.text.toFloat(),
                            startDate = startDate,
                            endDate = endDate,
                            isActive = false,
                            budgetedAmount = 0f,
                            spentAmount = 0f
                        )
                        CreateBudgetScreenViewModel.createBudget(budget) { budgetId ->
                            onNavigate(Screens.AddCategoryScreen.createRoute(budgetId))
                        }
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

fun convertMillisToDate(millis: Long): String {
    return Instant.fromEpochMilliseconds(epochMilliseconds = millis).toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
}
