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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
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
import androidx.compose.ui.input.key.Key.Companion.Calendar
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import co.ke.foxlysoft.budgetgain.database.BudgetEntity
import co.ke.foxlysoft.budgetgain.navigation.Screens
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateBudgetScreen(
    CreateBudgetScreenViewModel: CreateBudgetScreenViewModel = koinViewModel(),
    onNavigate: (String) -> Unit
) {
    var budgetName by remember { mutableStateOf(TextFieldValue("")) }
    var budgetAmount by remember { mutableStateOf(TextFieldValue("")) }
    var startDate by remember { mutableStateOf("Select Start Date") }
    var endDate by remember { mutableStateOf("Select End Date") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Track date picker dialog visibility
    val startDatePickerState = rememberDatePickerState()
    val endDatePickerState = rememberDatePickerState()
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val selectedStartDate = startDatePickerState.selectedDateMillis?.let {
        convertMillisToDate(it)
    } ?: ""
    val selectedEndDate = endDatePickerState.selectedDateMillis?.let {
        convertMillisToDate(it)
    } ?: ""

    // On Date Picker dialog result
    val onStartDateSet: (Int, Int, Int) -> Unit = { year, month, dayOfMonth ->

    }

    val onEndDateSet: (Int, Int, Int) -> Unit = { year, month, dayOfMonth ->

    }

    // Date picker dialog effects
    if (showStartDatePicker) {
        Popup(
            onDismissRequest = { showStartDatePicker = false },
            alignment = Alignment.TopStart
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = 64.dp)
                    .shadow(elevation = 4.dp)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
            ) {
                DatePicker(
                    state = startDatePickerState,
                    showModeToggle = false
                )
            }
        }
    }

    if (showEndDatePicker) {
        Popup(
            onDismissRequest = { showEndDatePicker = false },
            alignment = Alignment.TopStart
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = 64.dp)
                    .shadow(elevation = 4.dp)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
            ) {
                DatePicker(
                    state = endDatePickerState,
                    showModeToggle = false
                )
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Text(modifier = Modifier.padding(16.dp), text = "Create Budget Screen")
        Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column {
                OutlinedTextField(
                    value = budgetName,
                    onValueChange = { budgetName = it },
                    label = { Text("Budget Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                )
                OutlinedTextField(
                    value = budgetAmount,
                    onValueChange = { budgetAmount = it },
                    label = { Text("Budget Amount") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                )
                OutlinedTextField(
                    value = selectedStartDate,
                    onValueChange = { },
                    label = { Text("Start Date") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showStartDatePicker = !showStartDatePicker }) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Select date"
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                )
                OutlinedTextField(
                    value = selectedEndDate,
                    onValueChange = { },
                    label = { Text("End Date") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showEndDatePicker = !showEndDatePicker }) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Select date"
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(onClick = {
                        val budget = BudgetEntity(
                            name = budgetName.text,
                            initialBalance = budgetAmount.text.toFloat(),
                            startDate = selectedStartDate,
                            endDate = selectedEndDate,
                            isActive = false,
                            budgetedAmount = 0f,
                            spentAmount = 0f
                        )
                        CreateBudgetScreenViewModel.createBudget(budget)
                        onNavigate(Screens.AllBudgets.route)
                    }) {
                        Text(text = "Create Budget")
                    }
                    FilledTonalButton(onClick = {
                        showStartDatePicker = true
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