package co.ke.foxlysoft.budgetgain.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import budgetgain.composeapp.generated.resources.Res
import budgetgain.composeapp.generated.resources.ic_visibility
import co.ke.foxlysoft.budgetgain.ui.Theme.BudgetGainTheme
import co.ke.foxlysoft.budgetgain.utils.ErrorStatus
import co.ke.foxlysoft.budgetgain.utils.combineDateTimeMillis
import co.ke.foxlysoft.budgetgain.utils.dateMillisToString
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.todayIn
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.time.Clock


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BGainOutlineField(
    modifier: Modifier = Modifier,
    labelStr: String,
    Value: String = "",
    errorStatus: ErrorStatus,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    isPasswordField: Boolean = false,
    isMonthPicker: Boolean = false,
    isDatePicker: Boolean = false,
    isDateTimePicker: Boolean = false,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    leadingIcon: @Composable() (() -> Unit)? = null,
    trailingIcon: @Composable() (() -> Unit)? = null,
    onValueChange: ((String) -> Unit)? = null,
    onDateChange: ((millis: Long, dateStr: String) -> Unit)? = null,
    onTimeChange: ((hour: Int, minute: Int, fullTime: String) -> Unit)? = null,
    onDateTimeMillisChange: ((millis: Long) -> Unit)? = null,
    validator: ((String) -> Unit)? = null,
    submitAttempted: Boolean = false,
) {

    var hasInteracted by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    var showTimePicker by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState()

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    var showMonthPicker by remember { mutableStateOf(false) }



    // shared
    val bGainlabel = @Composable {
        Text(
            labelStr,
            style = MaterialTheme.typography.bodyMedium
        )
    }

    if (isDatePicker || isDateTimePicker) {
        // Date picker dialog effects
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        showDatePicker = false
                        datePickerState.selectedDateMillis?.let { millis ->
                            onDateChange?.invoke(millis, dateMillisToString(millis))
                            if (isDateTimePicker) showTimePicker = true
                        }

                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDatePicker = false
                    }) {
                        Text("Cancel")
                    }
                },
            ){
                DatePicker(
                    state = datePickerState,
                    showModeToggle = false
                )
            }
        }

        if (showTimePicker) {
            TimePickerDialog(
                onCancel = {
                    showTimePicker = false
                },
                onConfirm = {
                    showTimePicker = false
                    val hour = timePickerState.hour
                    val minute = timePickerState.minute
                    val fullTime = "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
                    onTimeChange?.invoke(hour, minute, fullTime) // Notify parent

                    // Calculate combined timestamp if date is already selected
                    datePickerState.selectedDateMillis?.let { dateMillis ->
                        val combinedMillis = combineDateTimeMillis(
                            dateMillis = dateMillis,
                            hour = hour,
                            minute = minute
                        )
                        onDateTimeMillisChange?.invoke(combinedMillis) // Notify parent
                    }
                },
            )
            {
                TimePicker(
                        state = timePickerState,
                    )
            }
        }

        OutlinedTextField(
            value = Value,
            onValueChange = { },
            label = bGainlabel,
            modifier = modifier,
            readOnly = true,
            isError = (submitAttempted || hasInteracted) && errorStatus.isError,
            supportingText = {
                if ((submitAttempted || hasInteracted) && errorStatus.isError) {
                    errorStatus.errorMsg?.let {
                        Text(
                            text = it, modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            leadingIcon = {
                IconButton(onClick = { showDatePicker = !showDatePicker }) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Select date time"
                    )
                }
            },
        )
    } else if (isMonthPicker) {
        if (showMonthPicker) {
            BasicAlertDialog(
                onDismissRequest = { showMonthPicker = false },
            ){
                MonthYearPickerContent(
                    monthYear = Value,
                    onMonthYearChange = {
                        onValueChange?.invoke(it)
                    },
                    onDismiss = { showMonthPicker = false}
                )
            }
        }
        OutlinedTextField(
            value = Value,
            onValueChange = {
                hasInteracted = true;
                if (validator != null) {
                    validator(it)
                };
                if (onValueChange != null) {
                    onValueChange(it)
                }
            },
            label = bGainlabel,
            modifier = modifier,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            isError = (submitAttempted || hasInteracted) && errorStatus.isError,
            supportingText = {
                if ((submitAttempted || hasInteracted) && errorStatus.isError) {
                    errorStatus.errorMsg?.let {
                        Text(
                            text = it, modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            leadingIcon = {
                IconButton(onClick = { showMonthPicker = !showMonthPicker }) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Pick month"
                    )
                }
            },
            trailingIcon = {
                if (Value != "") {
                    IconButton(onClick = { onValueChange?.invoke("") }) {
                        Icon(
                            imageVector = Icons.Outlined.Cancel,
                            contentDescription = "Pick month"
                        )
                    }
                }
            }
        )
    } else {
        OutlinedTextField(
            value = Value,
            onValueChange = {
                hasInteracted = true;
                if (validator != null) {
                    validator(it)
                };
                if (onValueChange != null) {
                    onValueChange(it)
                }
            },
            label = { Text(labelStr, style = MaterialTheme.typography.bodyMedium) },
            modifier = modifier,
            singleLine = true,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            leadingIcon = leadingIcon,
            isError = (submitAttempted || hasInteracted) && errorStatus.isError,
            supportingText = {
                if ((submitAttempted || hasInteracted) && errorStatus.isError) {
                    errorStatus.errorMsg?.let {
                        Text(
                            text = it, modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            trailingIcon = if (isPasswordField) {
                {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            painter = if (passwordVisible) painterResource(Res.drawable.ic_visibility)
                            //                        else painterResource(Res.drawable.ic_visibility_off),
                            else painterResource(Res.drawable.ic_visibility),
                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
                        )
                    }
                }
            } else if (trailingIcon != null) {
                trailingIcon
            } else if ((submitAttempted || hasInteracted) && errorStatus.isError) {
                {
                    Icon(imageVector = Icons.Filled.Info, contentDescription = null)
                }
            } else {
                null
            },
        )
    }
}

@Composable
@Preview
fun BGainOutlineFieldMonthPickerPreview() {
    BudgetGainTheme {
        Surface {
            Column(modifier = Modifier.fillMaxWidth(),) {
                BGainOutlineField(
                    modifier = Modifier
                        .fillMaxWidth(),
                    isMonthPicker = true,
                    labelStr = "Select Month",
                    Value = "",
                    errorStatus = ErrorStatus(isError = false),
                    onValueChange = { },
                )
            }

        }
    }
}

@Composable
fun MonthYearPickerContent(
    monthYear: String = "",
    onMonthYearChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val currentMonth = today.month.number
    val currentYear = today.year

    var splitMonthYear: List<Int> = listOf()
    splitMonthYear = if (monthYear != "") {
        monthYear.split("-").map { it.toInt() }
    } else {
        listOf(today.year, currentMonth)
    }

    var month by remember { mutableStateOf(splitMonthYear[1]) }
    var year by remember { mutableStateOf(splitMonthYear[0]) }
    var selectedYear by remember { mutableStateOf(splitMonthYear[0]) }

    val onYearChange = { ind: Int ->
        year = ind
        onMonthYearChange("$year-${month.toString().padStart(2, '0')}")
    }

    val onMonthClick = { ind: Int ->
        month = ind + 1
        selectedYear = year
        onMonthYearChange("$year-${month.toString().padStart(2, '0')}")
        onDismiss()
    }

    val months = listOf(
        listOf("Jan", "Feb", "Mar"),
        listOf("Apr", "May", "Jun"),
        listOf("Jul", "Aug", "Sep"),
        listOf("Oct", "Nov", "Dec")
    )
    Card {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                IconButton(onClick = { onYearChange(year - 1) }){
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Select date time"
                    )
                }
                Text(text = year.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = { onYearChange(year + 1) }){
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Select date time"
                    )
                }
            }
            HorizontalDivider()
            for ((i, monthRow) in months.withIndex()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    for ((j, m) in monthRow.withIndex()) {
                        val monthInd = i * 3 + j
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                month > 0 && monthInd == month - 1 && selectedYear == year -> Button(onClick = { onMonthClick(monthInd) }) {
                                    Text(text = m)
                                }
                                monthInd == currentMonth - 1 && year == currentYear -> OutlinedButton(onClick = { onMonthClick(monthInd) }) {
                                    Text(text = m)
                                }
                                else -> TextButton(onClick = { onMonthClick(monthInd) }) {
                                    Text(text = m)
                                }
                            }
                        }

                    }
                }
            }
        }
    }

}

@Composable
@Preview
fun MonthPickerContentPreview() {
    BudgetGainTheme {
        Surface {
            MonthYearPickerContent(
                onMonthYearChange = {},
                onDismiss = {}
            )
        }
    }
}

@Composable
@Preview
fun DarkMonthPickerContentPreview() {
    BudgetGainTheme(darkTheme = true) {
        Surface {
            MonthYearPickerContent(
                onMonthYearChange = {},
                onDismiss = {}
            )
        }
    }
}