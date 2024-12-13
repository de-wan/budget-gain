package co.ke.foxlysoft.budgetgain.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Popup
import budgetgain.composeapp.generated.resources.Res
import budgetgain.composeapp.generated.resources.ic_visibility
import co.ke.foxlysoft.budgetgain.utils.ErrorStatus
import co.ke.foxlysoft.budgetgain.utils.dateMillisToString
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.painterResource


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BGainOutlineField(
    modifier: Modifier = Modifier,
    labelStr: String,
    textFieldInput: TextFieldValue? = null,
    errorStatus: ErrorStatus,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    isPasswordField: Boolean = false,
    isDatePicker: Boolean = false,
    isDateTimePicker: Boolean = false,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    leadingIcon: @Composable() (() -> Unit)? = null,
    trailingIcon: @Composable() (() -> Unit)? = null,
    onValueChange: ((TextFieldValue) -> Unit)? = null,
    onDateChange: ((Long) -> Unit)? = null,
    onDateTimeChange: ((String) -> Unit)? = null,
    validator: ((String) -> Unit)? = null,
    submitAttempted: Boolean = false,
) {

    var hasInteracted by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    var showTimePicker by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState()
    val selectedTimeStr = {
        var result = "__:__"
        if (timePickerState.hour != 0 || timePickerState.minute != 0) {
            result = timePickerState.hour.toString().padStart(2, '0') + ":" + timePickerState.minute.toString().padStart(2, '0')
        }
        result
    }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    var selectedDateStr by remember { mutableStateOf("____-__-__") }



    // shared
    val bGainlabel = @Composable {
        Text(
            labelStr,
            style = MaterialTheme.typography.bodyMedium
        )
    }

    if (isDatePicker) {
        // Date picker dialog effects
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        showDatePicker = false
                        datePickerState.selectedDateMillis?.let {
                            if (onDateChange != null) {
                                onDateChange(it)
                            }
                            selectedDateStr = dateMillisToString(it)
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

        OutlinedTextField(
            value = selectedDateStr,
            onValueChange = { },
            label = bGainlabel,
            modifier = modifier,
            readOnly = true,
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
            trailingIcon = {
                IconButton(onClick = { showDatePicker = !showDatePicker }) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Select date"
                    )
                }
            },
        )
    } else if (isDateTimePicker) {
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        showDatePicker = false
                        showTimePicker = true
                        datePickerState.selectedDateMillis?.let {
                            if (onDateChange != null) {
                                onDateChange(it)
                            }
                            selectedDateStr = dateMillisToString(it)
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
                    onDateTimeChange?.invoke("$selectedDateStr ${selectedTimeStr()}")
                },
            )
            {
                TimePicker(
                        state = timePickerState,
                    )
            }
        }

        OutlinedTextField(
            value = "$selectedDateStr ${selectedTimeStr()}",
            onValueChange = { },
            label = bGainlabel,
            modifier = modifier,
            readOnly = true,
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
            trailingIcon = {
                IconButton(onClick = { showDatePicker = !showDatePicker }) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Select date time"
                    )
                }
            },
        )
    }
    else if (textFieldInput != null) {
        OutlinedTextField(
            value = textFieldInput,
            onValueChange = {
                hasInteracted = true;
                if (validator != null) {
                    validator(it.text)
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