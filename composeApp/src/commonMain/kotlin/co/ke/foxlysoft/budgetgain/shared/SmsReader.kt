package co.ke.foxlysoft.budgetgain.shared

expect class SmsReader() {
    fun getMpesaSms(fromDate: Long, toDate: Long): List<String>


}