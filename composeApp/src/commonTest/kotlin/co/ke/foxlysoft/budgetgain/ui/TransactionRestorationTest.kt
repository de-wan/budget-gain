package co.ke.foxlysoft.budgetgain.ui

import co.ke.foxlysoft.budgetgain.database.AccountEntity
import co.ke.foxlysoft.budgetgain.database.BudgetEntity
import co.ke.foxlysoft.budgetgain.database.CategoryEntity
import co.ke.foxlysoft.budgetgain.database.TransactionEntity
import kotlin.test.Test
import kotlin.test.assertEquals

class TransactionRestorationTest {
    @Test
    fun deletingTransactionReversesAllFinancialAggregates() {
        val transaction = TransactionEntity(amount = 2_500)
        val merchant = AccountEntity(balance = 10_000)
        val budgetAccount = AccountEntity(balance = 40_000)
        val category = CategoryEntity(
            budgetId = 1,
            name = "Food",
            amount = 20_000,
            spentAmount = 8_000,
        )
        val budget = BudgetEntity(spentAmount = 15_000)

        restoreTransactionAmounts(transaction, merchant, budgetAccount, category, budget)

        assertEquals(7_500L, merchant.balance)
        assertEquals(42_500L, budgetAccount.balance)
        assertEquals(5_500L, category.spentAmount)
        assertEquals(12_500L, budget.spentAmount)
    }
}
