package co.ke.foxlysoft.budgetgain.utils

import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime


data class TextFieldInput(
    var value: TextFieldValue = TextFieldValue(),
    var hasInteracted: Boolean = false,
)

data class FieldInput(
    val value: String = "",
    val hasInteracted: Boolean = false,
)

data class ErrorStatus(
    val isError: Boolean,
    val errorMsg: String? = null,
)

fun dateMillisToString(millis: Long): String {
    return Instant.fromEpochMilliseconds(epochMilliseconds = millis).toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
}

fun amountToCents(amount: String): Long {
    // Split the string into whole and fractional parts
    val parts = amount.split(".")
    val wholePart = parts[0] // Before the decimal point
    val fractionalPart = if (parts.size > 1) parts[1] else "0" // After the decimal point

    // Ensure the fractional part has exactly two digits
    val adjustedFractionalPart = fractionalPart.padEnd(2, '0').take(2)

    // Combine whole and fractional parts into cents
    return (wholePart + adjustedFractionalPart).toLong()
}

fun centsToString(cents: Long): String {
    // Separate whole dollars and fractional cents
    val dollars = cents / 100
    val fractionalCents = cents % 100

    // Format using string templates and pad fractional part to two digits
    return "$dollars.${fractionalCents.toString().padStart(2, '0')}"
}

fun isValidAmount(amount: String): Boolean {
    // Check if the amount is not empty and matches the regex pattern for valid amounts
    val regex = "^\\d+(\\.\\d{1,2})?$".toRegex()
    return amount.matches(regex)
}