package co.ke.foxlysoft.budgetgain.repos

import co.ke.foxlysoft.budgetgain.database.AppDatabase
import co.ke.foxlysoft.budgetgain.database.BudgetEntity

class BudgetRepository(
    db: AppDatabase
) {
    private val budgetDao = db.budgetDao()

    fun getAllBudgets() = budgetDao.getAll()

    suspend fun upsertBudget(budgetEntity: BudgetEntity) = budgetDao.upsert(budgetEntity)

    suspend fun deleteBudget(budgetEntity: BudgetEntity) = budgetDao.delete(budgetEntity)
}