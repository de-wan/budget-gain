package co.ke.foxlysoft.budgetgain.utils

import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.abs


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
    // Check if the amount is negative
    val isNegative = cents < 0

    // Work with absolute value of cents for formatting
    val absoluteCents = abs(cents)

    // Separate whole dollars and fractional cents
    val ints = absoluteCents / 100
    val fractionalCents = absoluteCents % 100

    // Format the ints with commas as thousand separators
    val formattedInts = formatWithCommas(ints)

    // Format using string templates and pad fractional part to two digits
    val formattedAmount = "$formattedInts.${fractionalCents.toString().padStart(2, '0')}"

    // If the original value was negative, prepend the minus sign
    return if (isNegative) "-$formattedAmount" else formattedAmount
}

fun formatWithCommas(number: Long): String {
    // Convert the number to a string
    val numberStr = number.toString()

    // Create a StringBuilder to insert commas
    val sb = StringBuilder()
    var counter = 0

    // Traverse the number from right to left and insert commas
    for (i in numberStr.length - 1 downTo 0) {
        sb.insert(0, numberStr[i])
        counter++
        if (counter % 3 == 0 && i != 0) {
            sb.insert(0, ",")
        }
    }

    return sb.toString()
}

fun isValidAmount(amount: String): Boolean {
    // Check if the amount is not empty and matches the regex pattern for valid amounts
    val regex = "^\\d+(\\.\\d{1,2})?$".toRegex()
    return amount.matches(regex)
}
