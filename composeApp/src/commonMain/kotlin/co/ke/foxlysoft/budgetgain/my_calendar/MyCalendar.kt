package co.ke.foxlysoft.budgetgain.my_calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import co.ke.foxlysoft.budgetgain.ui.Theme.BudgetGainTheme
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.yearMonth
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun MonthCalendar(
    year: Int,
    month: Int,
    onDayClick: (LocalDate) -> Unit,
    dayStatus: Map<LocalDate, DayStatus>
) {
    val firstOfMonth = LocalDate(year, month, 1)
    val daysInMonth = firstOfMonth.yearMonth.numberOfDays
    val startDayOfWeek = firstOfMonth.dayOfWeek.isoDayNumber % 7

    Column {
        WeekHeader()
        // 42 cells (6 weeks)
        val cells = (0 until 42).map { i ->
            val dayNum = i - startDayOfWeek + 1
            if (dayNum in 1..daysInMonth) LocalDate(year, month, dayNum) else null
        }
        cells.chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                week.forEach { date ->
                    DayCell(date, dayStatus[date], onDayClick)
                }
            }
        }
    }
}

@Preview
@Composable
fun MonthCalendarPreview (){
    var dayStatus = mutableMapOf<LocalDate, DayStatus>()
    dayStatus[LocalDate(2026, 2, 1)] = DayStatus(isMovedForward = true)
    dayStatus[LocalDate(2026, 2, 2)] = DayStatus(isMovedForward = true, isUsed = true)
    dayStatus[LocalDate(2026, 2, 3)] = DayStatus(isMovedForward = true, isUsed = true, isOverUsed = true)
    BudgetGainTheme {
        Surface {
            MonthCalendar(
                year = 2026,
                month = 1,
                onDayClick = {},
                dayStatus = dayStatus
            )
        }
    }
}

@Preview
@Composable
fun DarkMonthCalendarPreview (){
    var dayStatus = mutableMapOf<LocalDate, DayStatus>()
    dayStatus[LocalDate(2026, 2, 1)] = DayStatus(isMovedForward = true)
    dayStatus[LocalDate(2026, 2, 2)] = DayStatus(isUsed = true)
    dayStatus[LocalDate(2026, 2, 3)] = DayStatus(isOverUsed = true)
    BudgetGainTheme(darkTheme = true) {
        Surface {
            MonthCalendar(
                year = 2026,
                month = 2,
                onDayClick = {},
                dayStatus = dayStatus
            )
        }
    }
}