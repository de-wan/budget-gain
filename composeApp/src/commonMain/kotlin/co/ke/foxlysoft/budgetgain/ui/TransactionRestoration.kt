package co.ke.foxlysoft.budgetgain.ui

import co.ke.foxlysoft.budgetgain.database.AccountEntity
import co.ke.foxlysoft.budgetgain.database.BudgetEntity
import co.ke.foxlysoft.budgetgain.database.CategoryEntity
import co.ke.foxlysoft.budgetgain.database.TransactionEntity

internal fun restoreTransactionAmounts(
    transaction: TransactionEntity,
    creditAccount: AccountEntity,
    debitAccount: AccountEntity,
    category: CategoryEntity,
    budget: BudgetEntity,
) {
    creditAccount.balance -= transaction.amount
    debitAccount.balance += transaction.amount
    category.spentAmount -= transaction.amount
    budget.spentAmount -= transaction.amount
}
