package co.ke.foxlysoft.budgetgain.my_calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import co.ke.foxlysoft.budgetgain.ui.Theme.BudgetGainTheme
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.yearMonth
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun MonthCalendar(
    year: Int,
    month: Int,
    onDayClick: (LocalDate) -> Unit,
    statusByDate: Map<LocalDate, DayStatus>,
    selectedDate: LocalDate? = null
) {
    val calendarDates = remember(year, month) { monthCalendarCells(year, month) }

    Column(
        modifier = Modifier.padding(top = 16.dp)
    ) {
        WeekHeader()
        // 42 cells (6 weeks)
        calendarDates.chunked(7).forEach { weekDates ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                weekDates.forEach { date ->
                    DayCell(
                        date = date,
                        dayStatus = date?.let(statusByDate::get),
                        isSelected = date != null && date == selectedDate,
                        onDayClick = onDayClick
                    )
                }
            }
        }
    }
}

internal fun monthCalendarCells(year: Int, month: Int): List<LocalDate?> {
    val firstOfMonth = LocalDate(year, month, 1)
    val daysInMonth = firstOfMonth.yearMonth.numberOfDays
    val startDayOfWeek = firstOfMonth.dayOfWeek.isoDayNumber % 7

    return (0 until 42).map { index ->
        val day = index - startDayOfWeek + 1
        if (day in 1..daysInMonth) LocalDate(year, month, day) else null
    }
}

@Preview
@Composable
fun MonthCalendarPreview (){
    val statusByDate = mapOf(
        LocalDate(2026, 2, 1) to DayStatus(isMovedForward = true),
        LocalDate(2026, 2, 2) to DayStatus(isMovedForward = true, isUsed = true),
        LocalDate(2026, 2, 3) to DayStatus(isMovedForward = true, isUsed = true, isOverUsed = true)
    )
    BudgetGainTheme {
        Surface {
            MonthCalendar(
                year = 2026,
                month = 1,
                onDayClick = {},
                statusByDate = statusByDate
            )
        }
    }
}

@Preview
@Composable
fun DarkMonthCalendarPreview (){
    val statusByDate = mapOf(
        LocalDate(2026, 2, 1) to DayStatus(isMovedForward = true),
        LocalDate(2026, 2, 2) to DayStatus(isUsed = true),
        LocalDate(2026, 2, 3) to DayStatus(isOverUsed = true)
    )
    BudgetGainTheme(darkTheme = true) {
        Surface {
            MonthCalendar(
                year = 2026,
                month = 2,
                onDayClick = {},
                statusByDate = statusByDate
            )
        }
    }
}
