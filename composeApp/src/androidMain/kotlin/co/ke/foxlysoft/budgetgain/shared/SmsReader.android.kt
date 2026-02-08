package co.ke.foxlysoft.budgetgain.shared

import android.net.Uri
import co.ke.foxlysoft.budgetgain.ContextProvider
import co.ke.foxlysoft.budgetgain.utils.dateMillisToString

actual class SmsReader {
    actual fun getMpesaSms(fromDate: Long, toDate: Long, searchQuery: String?): List<String> {
        val context = ContextProvider.context;

        println("From date: ${dateMillisToString(fromDate)}, To date: ${dateMillisToString(toDate)}")
        val search = searchQuery ?: ""

        val smsList = mutableListOf<String>()
        val uri = Uri.parse("content://sms/inbox")
        val projection = arrayOf("_id", "address", "body", "date")
        val selection = "address = ? AND date >= ? AND date <= ? AND body LIKE ?"
        val selectionArgs = arrayOf("MPESA", fromDate.toString(), toDate.toString(), "%$search%")

        val cursor = context.contentResolver.query(
            uri, projection, selection, selectionArgs, "date"
        )
        cursor?.use {
            var smsCount = 0
            while (it.moveToNext()) {
                val date = it.getString(it.getColumnIndexOrThrow("date"))
                val body = it.getString(it.getColumnIndexOrThrow("body"))
                smsList.add(body)
                smsCount++
            }
            println("Sms count: $smsCount")
        }
        return smsList
    }
}