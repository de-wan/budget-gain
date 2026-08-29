package co.ke.foxlysoft.budgetgain.my_calendar

data class DayStatus(
    val broughtForwardAmount: Long = 0,

    val isMovedForward: Boolean = false,
    val carriedForwardAmount: Long = 0,

    val isUsed: Boolean = false,
    val usedAmount: Long = 0,

    val isOverUsed: Boolean = false,
    val overUsedAmount: Long = 0
)
