package co.ke.foxlysoft.budgetgain.my_calendar

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class MonthCalendarTest {
    @Test
    fun leapMonthUsesSixWeekSundayFirstGrid() {
        val cells = monthCalendarCells(2024, 2)

        assertEquals(42, cells.size)
        assertEquals(LocalDate(2024, 2, 1), cells[4])
        assertEquals(LocalDate(2024, 2, 29), cells[32])
        assertEquals(29, cells.count { it != null })
    }

    @Test
    fun monthStartingSundayHasNoLeadingBlankCell() {
        val cells = monthCalendarCells(2026, 2)

        assertEquals(LocalDate(2026, 2, 1), cells.first())
    }
}
