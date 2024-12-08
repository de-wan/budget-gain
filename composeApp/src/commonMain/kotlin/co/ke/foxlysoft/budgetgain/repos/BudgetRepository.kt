package co.ke.foxlysoft.budgetgain.repos

import co.ke.foxlysoft.budgetgain.database.AppDatabase
import co.ke.foxlysoft.budgetgain.database.BudgetEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

class BudgetRepository(
    db: AppDatabase
) {
    private val budgetDao = db.budgetDao()

    fun getAllBudgets() = budgetDao.getAll()

    suspend fun upsertBudget(budgetEntity: BudgetEntity) = budgetDao.upsert(budgetEntity)

    suspend fun deleteBudget(budgetEntity: BudgetEntity) = budgetDao.delete(budgetEntity)

    fun getCurrentBudget(): Flow<BudgetEntity> {
        return budgetDao.getCurrentBudget()
            .mapNotNull { it }
    }

    suspend fun countBudgets(): Int {
        return budgetDao.countBudgets()
    }

    suspend fun incrementBudgetedAmount(budgetId: Long, incrementBy: Long) {
        val curBudget = budgetDao.getBudget(budgetId)
        curBudget.budgetedAmount += incrementBy
        upsertBudget(curBudget)
    }
}