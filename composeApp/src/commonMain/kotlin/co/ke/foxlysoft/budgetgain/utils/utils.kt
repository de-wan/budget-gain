package co.ke.foxlysoft.budgetgain.utils

import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.abs
import kotlin.math.max


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

fun timeMillisToString(millis: Long): String {
    return Instant.fromEpochMilliseconds(epochMilliseconds = millis).toLocalDateTime(TimeZone.currentSystemDefault()).time.toString()
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

fun isDigit(c: Char): Boolean {
    return c in '0'..'9'
}

fun isValidAmount(amount: String): Boolean {
    // Check if the amount is not empty and matches the regex pattern for valid amounts
    val regex = "^\\d+(\\.\\d{1,2})?$".toRegex()
    return amount.matches(regex)
}

fun smsParser(sms: String) {
    var rules = LinkedHashMap<String, String>()
    rules["S"] = "[Ref] Confirmed. Ksh(send,till,pochi,paybill)|[Ref] Confirmed. You have received Ksh[Amount] from [Sender] [Phone] on [Date] at [Time] New M-PESA balance is Ksh[Amount].(receive)|[Ref] Confirmed.on (withdraw)|[Ref] Confirmed. Your M-Shwari loan has been approved on [Datetime] and Ksh[Amount] less excise duty has been deposited to your M-PESA account. New M-PESA balance is Ksh[Balance].(mshwariLoan)"
    rules["Confirmed. Ksh(send,till,pochi,paybill)"] = "[Amount] (send,till,pochi,paybill)*"//
    rules["Confirmed. You have received Ksh(receive)"] = "[Amount] (receive)"
    rules["Confirmed.on (withdraw)"] = "[Datetime]Withdraw Ksh[Amount] from [Agent] - [AgentName] New M-PESA balance is Ksh[Balance]. Transaction cost, Ksh[Cost]."
    rules["[Amount] (send,till,pochi,paybill)"] = "sent to [Recipient] [Phone] on [Date] at [Time]. New M-PESA balance is Ksh[Balance].(send)|sent to [Recipient] on [Date] at [Time]. New M-PESA balance is Ksh[Amount].(pochi)|paid to [Merchant]. on [Date] at [Time]. New M-PESA balance is Ksh[Balance](till)"

    // extract send

}

fun extractSendSms(sms: String): MpesaSms {
    // specify sms type before the final return
    val mpesaSms = MpesaSms(
        smsType = MpesaSmsTypes.UNKNOWN,
        ref = "",
        amount = 0L,
        date = "",
        time = "",
        subjectPrimaryIdentifierType = "",
        subjectPrimaryIdentifier = "",
        subjectSecondaryIdentifierType = "",
        subjectSecondaryIdentifier = "",
        cost = 0L,
        balance = 0L
    )

    var i = 0
    // [Ref]
    val (word, newI, ok) = extractWord(sms, i)
    if (!ok) {
        return mpesaSms
    }
    mpesaSms.ref = word
    i = newI

    // before amount
    var x = sms.substring(i, i+15)
    if (x != " Confirmed. Ksh") {
        return mpesaSms
    }
    i+=15

    // extract amount
    val (rawAmount, newInd, amountOk) = extractAmount(sms, i)
    if (!amountOk) {
        return mpesaSms
    }
    mpesaSms.amount = amountToCents(rawAmount)
    i = newInd

    x = sms.substring(i, i+9)
    if (x != " sent to ") {
        return mpesaSms
    }
    i+=9

    val recipientName = StringBuilder()
    // loop until we find a digit or plus
    while (i < sms.length && !isDigit(sms[i]) && sms[i] != '+') {
        recipientName.append(sms[i])
        i++
    }
    // pop the last character
    recipientName.setLength(max(recipientName.length - 1, 0))
    mpesaSms.subjectSecondaryIdentifierType = "name"
    mpesaSms.subjectSecondaryIdentifier = recipientName.toString()

    // phone number
    val phoneNumberBuilder = StringBuilder()
    while(i < sms.length && isDigit(sms[i])) {
        phoneNumberBuilder.append(sms[i])
        i++
    }
    mpesaSms.subjectPrimaryIdentifierType = "phone"
    mpesaSms.subjectPrimaryIdentifier = phoneNumberBuilder.toString()

    x = sms.substring(i, i+4)
    if (x != " on ") {
        return mpesaSms
    }
    i += 4

    // date
    val dateBuilder = StringBuilder()
    while(i < sms.length && sms[i] != ' ') {
        dateBuilder.append(sms[i])
        i++
    }
    mpesaSms.date = dateBuilder.toString()

    x = sms.substring(i, i+4)
    if (x != " at ") {
        return mpesaSms
    }
    i += 4

    // time
    val timeBuilder = StringBuilder()
    while(i < sms.length && sms[i] != '.') {
        timeBuilder.append(sms[i])
        i++
    }
    mpesaSms.time = timeBuilder.toString()

    x = sms.substring(i, i+27)
    if (x != ". New M-PESA balance is Ksh") {
        return mpesaSms
    }
    i += 27

    // extract balance
    val (rawBalance, balInd, balOk) = extractAmount(sms, i)
    if (!balOk) {
        return mpesaSms
    }
    mpesaSms.balance = amountToCents(rawBalance)
    i = balInd


    x = sms.substring(i, i + 23)
    if (x != ". Transaction cost, Ksh") {
        return mpesaSms
    }
    i += 23

    // extract cost
    val (rawCost, costInd, costOk) = extractAmount(sms, i)
    if (!costOk) {
        return mpesaSms
    }
    mpesaSms.cost = amountToCents(rawCost)
    i = costInd

    mpesaSms.smsType = MpesaSmsTypes.SEND_MONEY

    return mpesaSms
}


fun extractReceiveSms(sms: String): MpesaSms {
    // specify sms type before the final return
    val mpesaSms = MpesaSms(
        smsType = MpesaSmsTypes.UNKNOWN,
        ref = "",
        amount = 0L,
        date = "",
        time = "",
        subjectPrimaryIdentifierType = "",
        subjectPrimaryIdentifier = "",
        subjectSecondaryIdentifierType = "",
        subjectSecondaryIdentifier = "",
        cost = 0L,
        balance = 0L
    )

    var i = 0
    // [Ref]
    val (word, newI, ok) = extractWord(sms, i)
    if (!ok) {
        return mpesaSms
    }
    mpesaSms.ref = word
    i = newI

    // before amount
    var x = sms.substring(i, i+32)
    if (x != " Confirmed.You have received Ksh") {
        return mpesaSms
    }
    i+=32

    // extract amount
    val (rawAmount, newInd, amountOk) = extractAmount(sms, i)
    if (!amountOk) {
        return mpesaSms
    }
    mpesaSms.amount = amountToCents(rawAmount)
    i = newInd

    x = sms.substring(i, i+6)
    if (x != " from ") {
        return mpesaSms
    }
    i+=6

    val senderName = StringBuilder()
    // loop until we find a digit or plus or followed by " on "
    while (i < sms.length && !isDigit(sms[i]) && sms[i] != '+' && !followedBy(sms, i, " on ")) {
        senderName.append(sms[i])
        i++
    }
    // pop the last character
    senderName.setLength(senderName.length - 1)
    mpesaSms.subjectSecondaryIdentifierType = "name"
    mpesaSms.subjectSecondaryIdentifier = senderName.toString()

    if (!followedBy(sms, i, " on ")) {
        // phone number
        val phoneNumberBuilder = StringBuilder()
        while(i < sms.length && isDigit(sms[i])) {
            phoneNumberBuilder.append(sms[i])
            i++
        }
        mpesaSms.subjectPrimaryIdentifierType = "phone"
        mpesaSms.subjectPrimaryIdentifier = phoneNumberBuilder.toString()

        x = sms.substring(i, i+4)
        if (x != " on ") {
            return mpesaSms
        }
        i += 4
    }



    // date
    val dateBuilder = StringBuilder()
    while(i < sms.length && sms[i] != ' ') {
        dateBuilder.append(sms[i])
        i++
    }
    mpesaSms.date = dateBuilder.toString()

    x = sms.substring(i, i+4)
    if (x != " at ") {
        return mpesaSms
    }
    i += 4

    // time
    val timeBuilder = StringBuilder()
    while(i < sms.length && !followedBy(sms, i, " New")) {
        timeBuilder.append(sms[i])
        i++
    }
    mpesaSms.time = timeBuilder.toString()

    x = sms.substring(i, i+26)
    if (x != " New M-PESA balance is Ksh") {
        return mpesaSms
    }
    i += 26

    // extract balance
    val (rawBalance, balInd, balOk) = extractAmount(sms, i)
    if (!balOk) {
        return mpesaSms
    }
    mpesaSms.balance = amountToCents(rawBalance)
    i = balInd

    mpesaSms.smsType = MpesaSmsTypes.RECEIVE_MONEY

    return mpesaSms
}

fun extractPochiSms(sms: String): MpesaSms {
    // specify sms type before the final return
    val mpesaSms = MpesaSms(
        smsType = MpesaSmsTypes.UNKNOWN,
        ref = "",
        amount = 0L,
        date = "",
        time = "",
        subjectPrimaryIdentifierType = "",
        subjectPrimaryIdentifier = "",
        subjectSecondaryIdentifierType = "",
        subjectSecondaryIdentifier = "",
        cost = 0L,
        balance = 0L
    )

    var i = 0
    // [Ref]
    val (word, newI, ok) = extractWord(sms, i)
    if (!ok) {
        return mpesaSms
    }
    mpesaSms.ref = word
    i = newI

    // before amount
    var x = sms.substring(i, i + 15)
    if (x != " Confirmed. Ksh") {
        return mpesaSms
    }
    i += 15

    // extract amount
    val (rawAmount, newInd, amountOk) = extractAmount(sms, i)
    if (!amountOk) {
        return mpesaSms
    }
    mpesaSms.amount = amountToCents(rawAmount)
    i = newInd

    x = sms.substring(i, i + 9)
    if (x != " sent to ") {
        return mpesaSms
    }
    i += 9

    val recipientName = StringBuilder()
    // loop until we find a digit or plus or followed by " on "
    while (i < sms.length && !isDigit(sms[i]) && sms[i] != '+' && !followedBy(sms, i, " on ")) {
        recipientName.append(sms[i])
        i++
    }
    // pop the last character
    mpesaSms.subjectPrimaryIdentifierType = "name"
    mpesaSms.subjectPrimaryIdentifier = recipientName.toString()

    x = sms.substring(i, i+4)
    if (x != " on ") {
        return mpesaSms
    }
    i += 4

    // date
    val dateBuilder = StringBuilder()
    while(i < sms.length && sms[i] != ' ') {
        dateBuilder.append(sms[i])
        i++
    }
    mpesaSms.date = dateBuilder.toString()

    x = sms.substring(i, i+4)
    if (x != " at ") {
        return mpesaSms
    }
    i += 4

    // time
    val timeBuilder = StringBuilder()
    while(i < sms.length && sms[i] != '.') {
        timeBuilder.append(sms[i])
        i++
    }
    mpesaSms.time = timeBuilder.toString()

    x = sms.substring(i, i+27)
    if (x != ". New M-PESA balance is Ksh") {
        return mpesaSms
    }
    i += 27

    // extract balance
    val (rawBalance, balInd, balOk) = extractAmount(sms, i)
    if (!balOk) {
        return mpesaSms
    }
    mpesaSms.balance = amountToCents(rawBalance)
    i = balInd


    x = sms.substring(i, i + 23)
    if (x != ". Transaction cost, Ksh") {
        return mpesaSms
    }
    i += 23

    // extract cost
    val (rawCost, costInd, costOk) = extractAmount(sms, i)
    if (!costOk) {
        return mpesaSms
    }
    mpesaSms.cost = amountToCents(rawCost)
    i = costInd

    mpesaSms.smsType = MpesaSmsTypes.POCHI

    return mpesaSms
}

fun extractTillSms(sms: String): MpesaSms {
    // specify sms type before the final return
    val mpesaSms = MpesaSms(
        smsType = MpesaSmsTypes.UNKNOWN,
        ref = "",
        amount = 0L,
        date = "",
        time = "",
        subjectPrimaryIdentifierType = "",
        subjectPrimaryIdentifier = "",
        subjectSecondaryIdentifierType = "",
        subjectSecondaryIdentifier = "",
        cost = 0L,
        balance = 0L
    )

    var i = 0
    // [Ref]
    val (word, newI, ok) = extractWord(sms, i)
    if (!ok) {
        return mpesaSms
    }
    mpesaSms.ref = word
    i = newI

    // before amount
    var x = sms.substring(i, i + 15)
    if (x != " Confirmed. Ksh") {
        return mpesaSms
    }
    i += 15

    // extract amount
    val (rawAmount, newInd, amountOk) = extractAmount(sms, i)
    if (!amountOk) {
        return mpesaSms
    }
    mpesaSms.amount = amountToCents(rawAmount)
    i = newInd

    x = sms.substring(i, i + 9)
    if (x != " paid to ") {
        return mpesaSms
    }
    i += 9

    val recipientName = StringBuilder()
    // loop until we find a digit or plus or followed by ". on "
    while (i < sms.length && !isDigit(sms[i]) && sms[i] != '+' && !followedBy(sms, i, ". on ")) {
        recipientName.append(sms[i])
        i++
    }
    // pop the last character
    mpesaSms.subjectPrimaryIdentifierType = "name"
    mpesaSms.subjectPrimaryIdentifier = recipientName.toString()

    x = sms.substring(i, i+5)
    if (x != ". on ") {
        return mpesaSms
    }
    i += 5

    // date
    val dateBuilder = StringBuilder()
    while(i < sms.length && sms[i] != ' ') {
        dateBuilder.append(sms[i])
        i++
    }
    mpesaSms.date = dateBuilder.toString()

    x = sms.substring(i, i+4)
    if (x != " at ") {
        return mpesaSms
    }
    i += 4

    // time
    val timeBuilder = StringBuilder()
    while(i < sms.length && sms[i] != '.') {
        timeBuilder.append(sms[i])
        i++
    }
    mpesaSms.time = timeBuilder.toString()

    x = sms.substring(i, i+26)
    if (x != ".New M-PESA balance is Ksh") {
        return mpesaSms
    }
    i += 26

    // extract balance
    val (rawBalance, balInd, balOk) = extractAmount(sms, i)
    if (!balOk) {
        return mpesaSms
    }
    mpesaSms.balance = amountToCents(rawBalance)
    i = balInd


    x = sms.substring(i, i + 23)
    if (x != ". Transaction cost, Ksh") {
        return mpesaSms
    }
    i += 23

    // extract cost
    val (rawCost, costInd, costOk) = extractAmount(sms, i)
    if (!costOk) {
        return mpesaSms
    }
    mpesaSms.cost = amountToCents(rawCost)
    i = costInd

    mpesaSms.smsType = MpesaSmsTypes.TILL

    return mpesaSms
}

fun extractPaybillSms(sms: String): MpesaSms {
    // specify sms type before the final return
    val mpesaSms = MpesaSms(
        smsType = MpesaSmsTypes.UNKNOWN,
        ref = "",
        amount = 0L,
        date = "",
        time = "",
        subjectPrimaryIdentifierType = "",
        subjectPrimaryIdentifier = "",
        subjectSecondaryIdentifierType = "",
        subjectSecondaryIdentifier = "",
        cost = 0L,
        balance = 0L
    )

    var i = 0
    // [Ref]
    val (word, newI, ok) = extractWord(sms, i)
    if (!ok) {
        return mpesaSms
    }
    mpesaSms.ref = word
    i = newI

    // before amount
    var x = sms.substring(i, i + 15)
    if (x != " Confirmed. Ksh") {
        return mpesaSms
    }
    i += 15

    // extract amount
    val (rawAmount, newInd, amountOk) = extractAmount(sms, i)
    if (!amountOk) {
        return mpesaSms
    }
    mpesaSms.amount = amountToCents(rawAmount)
    i = newInd

    x = sms.substring(i, i + 9)
    if (x != " sent to ") {
        return mpesaSms
    }
    i += 9

    val recipientName = StringBuilder()
    // loop until we find a digit or plus or followed by ". on "
    while (i < sms.length && !followedBy(sms, i, " for account ")) {
        recipientName.append(sms[i])
        i++
    }
    // pop the last character
    mpesaSms.subjectPrimaryIdentifierType = "paybill"
    mpesaSms.subjectPrimaryIdentifier = recipientName.toString()

    x = sms.substring(i, i+13)
    if (x != " for account ") {
        return mpesaSms
    }
    i += 13

    val recipientAccount = StringBuilder()
    // loop until we find a digit or plus or followed by ". on "
    while (i < sms.length && !followedBy(sms, i, " on ")) {
        recipientAccount.append(sms[i])
        i++
    }
    // pop the last character
    mpesaSms.subjectSecondaryIdentifierType = "account"
    mpesaSms.subjectSecondaryIdentifier = recipientAccount.toString()

    x = sms.substring(i, i+4)
    if (x != " on ") {
        return mpesaSms
    }
    i += 4

    // date
    val dateBuilder = StringBuilder()
    while(i < sms.length && sms[i] != ' ') {
        dateBuilder.append(sms[i])
        i++
    }
    mpesaSms.date = dateBuilder.toString()

    x = sms.substring(i, i+4)
    if (x != " at ") {
        return mpesaSms
    }
    i += 4

    // time
    val timeBuilder = StringBuilder()
    while(i < sms.length && !followedBy(sms, i, " New")) {
        timeBuilder.append(sms[i])
        i++
    }
    mpesaSms.time = timeBuilder.toString()

    x = sms.substring(i, i+26)
    if (x != " New M-PESA balance is Ksh") {
        return mpesaSms
    }
    i += 26

    // extract balance
    val (rawBalance, balInd, balOk) = extractAmount(sms, i)
    if (!balOk) {
        return mpesaSms
    }
    mpesaSms.balance = amountToCents(rawBalance)
    i = balInd


    x = sms.substring(i, i + 23)
    if (x != ". Transaction cost, Ksh") {
        return mpesaSms
    }
    i += 23

    // extract cost
    val (rawCost, costInd, costOk) = extractAmount(sms, i)
    if (!costOk) {
        return mpesaSms
    }
    mpesaSms.cost = amountToCents(rawCost)
    i = costInd

    mpesaSms.smsType = MpesaSmsTypes.PAYBILL

    return mpesaSms
}

fun extractWithdrawSms(sms: String): MpesaSms {
    // specify sms type before the final return
    val mpesaSms = MpesaSms(
        smsType = MpesaSmsTypes.UNKNOWN,
        ref = "",
        amount = 0L,
        date = "",
        time = "",
        subjectPrimaryIdentifierType = "",
        subjectPrimaryIdentifier = "",
        subjectSecondaryIdentifierType = "",
        subjectSecondaryIdentifier = "",
        cost = 0L,
        balance = 0L
    )

    var i = 0
    // [Ref]
    val (word, newI, ok) = extractWord(sms, i)
    if (!ok) {
        return mpesaSms
    }
    mpesaSms.ref = word
    i = newI

    // before amount
    var x = sms.substring(i, i + 14)
    if (x != " Confirmed.on ") {
        return mpesaSms
    }
    i += 14

    // date
    val dateBuilder = StringBuilder()
    while(i < sms.length && sms[i] != ' ') {
        dateBuilder.append(sms[i])
        i++
    }
    mpesaSms.date = dateBuilder.toString()

    x = sms.substring(i, i+4)
    if (x != " at ") {
        return mpesaSms
    }
    i += 4

    // time
    val timeBuilder = StringBuilder()
    while(i < sms.length && sms[i] != 'W') {
        timeBuilder.append(sms[i])
        i++
    }
    mpesaSms.time = timeBuilder.toString()

    x = sms.substring(i, i+12)
    if (x != "Withdraw Ksh") {
        return mpesaSms
    }
    i += 12

    // extract amount
    val (rawAmount, newInd, amountOk) = extractAmount(sms, i)
    if (!amountOk) {
        return mpesaSms
    }
    mpesaSms.amount = amountToCents(rawAmount)
    i = newInd

    x = sms.substring(i, i + 6)
    if (x != " from ") {
        return mpesaSms
    }
    i += 6

    val recipientTill = StringBuilder()
    // loop until we find a non-digit
    while (i < sms.length && isDigit(sms[i])) {
        recipientTill.append(sms[i])
        i++
    }
    // pop the last character
    mpesaSms.subjectPrimaryIdentifierType = "till"
    mpesaSms.subjectPrimaryIdentifier = recipientTill.toString()

    x = sms.substring(i, i + 3)
    if (x != " - ") {
        return mpesaSms
    }
    i += 3

    val recipientName = StringBuilder()
    // loop until we find a digit or plus or followed by ". on "
    while (i < sms.length && !isDigit(sms[i]) && sms[i] != '+' && !followedBy(sms, i, " New")) {
        recipientName.append(sms[i])
        i++
    }
    // pop the last character
    mpesaSms.subjectSecondaryIdentifierType = "name"
    mpesaSms.subjectSecondaryIdentifier = recipientName.toString()

    x = sms.substring(i, i+26)
    if (x != " New M-PESA balance is Ksh") {
        return mpesaSms
    }
    i += 26

    // extract balance
    val (rawBalance, balInd, balOk) = extractAmount(sms, i)
    if (!balOk) {
        return mpesaSms
    }
    mpesaSms.balance = amountToCents(rawBalance)
    i = balInd


    x = sms.substring(i, i + 23)
    if (x != ". Transaction cost, Ksh") {
        return mpesaSms
    }
    i += 23

    // extract cost
    val (rawCost, costInd, costOk) = extractAmount(sms, i)
    if (!costOk) {
        return mpesaSms
    }
    mpesaSms.cost = amountToCents(rawCost)
    i = costInd

    mpesaSms.smsType = MpesaSmsTypes.WITHDRAW_MONEY

    return mpesaSms
}


fun extractWord(sms: String, ind: Int): Triple<String, Int, Boolean> {
    var newInd = ind
    var wordFound = false
    var word = ""
    var char = sms[newInd]
    while (char != ' ') {
        wordFound = true
        word += char
        newInd ++
        char = sms[newInd]
    }

    return Triple<String, Int, Boolean>(word,newInd, wordFound)
}

fun extractAmount(sms: String, ind: Int): Triple<String, Int, Boolean> {
    var newInd = ind
    var wordFound = false
    val word = StringBuilder()

    var decimalFound = false

    while (newInd < sms.length && sms[newInd] != ' ') {
        if (sms[newInd] == ',') {
            newInd ++
            continue
        }

        if (sms[newInd] == '.' && decimalFound) {
            break
        }

        if (sms[newInd] == '.') {
            decimalFound = true
        }

        wordFound = true
        word.append(sms[newInd])
        newInd ++
    }

    return Triple<String, Int, Boolean>(word.toString(), newInd, wordFound)
}

fun followedBy(sms: String, ind: Int, needle: String): Boolean {
    var i = 0

    if (needle.isEmpty()){
        return false
    }

    if (sms.length - ind == 0) {
        return false
    }

    while(ind+i<sms.length && i < needle.length){
        val cx = sms[ind+i]
        val cy = needle[i]
        if(cx != cy){
            return false
        }
        i += 1
    }

    return true
}

fun subArray(str: String, start: Int, end: Int): String {
    return str.substring(start, end)
}
