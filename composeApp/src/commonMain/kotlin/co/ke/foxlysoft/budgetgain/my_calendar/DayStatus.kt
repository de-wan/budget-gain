package co.ke.foxlysoft.budgetgain.my_calendar

data class DayStatus(
    val isMovedForward: Boolean = false,
    val isUsed: Boolean = false,
    val isOverUsed: Boolean = false
)