package co.ke.foxlysoft.budgetgain.ui

import co.ke.foxlysoft.budgetgain.database.TransactionEntity
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class DailyUsageCalculationTest {
    @Test
    fun exclusionAndRestorationRecalculateFollowingDays() {
        val purchase = TransactionEntity(amount = 45_000)
        val included = calculateDailyUsageStatuses(2026, 10, longArrayOf(0, purchase.dailyUsageAmount(), 10_000), 30_000)
        val excluded = calculateDailyUsageStatuses(2026, 10, longArrayOf(0, purchase.copy(excludedFromDailyUsage = true).dailyUsageAmount(), 10_000), 30_000)
        assertEquals(15_000L, included.getValue(LocalDate(2026, 10, 1)).overUsedAmount)
        assertEquals(-15_000L, included.getValue(LocalDate(2026, 10, 2)).broughtForwardAmount)
        assertEquals(30_000L, excluded.getValue(LocalDate(2026, 10, 2)).broughtForwardAmount)
        assertEquals(50_000L, excluded.getValue(LocalDate(2026, 10, 2)).carriedForwardAmount)
        assertFalse(excluded.getValue(LocalDate(2026, 10, 1)).isUsed)
        assertFalse(excluded.getValue(LocalDate(2026, 10, 1)).isOverUsed)
        val restored = purchase.copy(excludedFromDailyUsage = true).copy(excludedFromDailyUsage = false)
        assertEquals(included, calculateDailyUsageStatuses(2026, 10, longArrayOf(0, restored.dailyUsageAmount(), 10_000), 30_000))
        assertEquals(45_000L, purchase.amount)
    }
}
