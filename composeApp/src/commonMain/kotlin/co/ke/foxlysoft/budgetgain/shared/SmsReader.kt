package co.ke.foxlysoft.budgetgain.shared

expect class SmsReader() {
    fun getMpesaSms(fromDate: Long, toDate: Long, searchQuery: String? = null): List<String>


}