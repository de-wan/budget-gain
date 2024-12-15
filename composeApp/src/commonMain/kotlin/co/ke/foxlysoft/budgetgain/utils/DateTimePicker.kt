package co.ke.foxlysoft.budgetgain.utils

import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerState

data class DateTimePickerState @OptIn(ExperimentalMaterial3Api::class) constructor(
    var datePickerState: DatePickerState,
    var timePickerState: TimePickerState,
    var selectedDateMillis: Long? = null,
    var selectedTimeMillis: Long? = null,
) {
    fun formattedDate(): String = selectedDateMillis?.let { dateMillisToString(it) } ?: ""
    fun formattedTime(): String = selectedTimeMillis?.let { timeMillisToString(it) } ?: ""
    fun formattedDateTime(): String = "${formattedDate()} ${formattedTime()}"
}
