package co.ke.foxlysoft.budgetgain.repos

import co.ke.foxlysoft.budgetgain.database.AppDatabase
import co.ke.foxlysoft.budgetgain.database.BudgetEntity
import kotlinx.coroutines.flow.Flow

class BudgetRepository(
    db: AppDatabase
) {
    private val budgetDao = db.budgetDao()

    fun getAllBudgets() = budgetDao.getAll()

    suspend fun upsertBudget(budgetEntity: BudgetEntity): Long = budgetDao.upsert(budgetEntity)

    suspend fun deleteBudget(budgetEntity: BudgetEntity) = budgetDao.delete(budgetEntity)

    fun getCurrentBudget(onStarted: () -> Unit, onComplete: (Flow<BudgetEntity?>) -> Unit) {
        onStarted();
        val currentBudget = budgetDao.getCurrentBudgetFlow()
        onComplete(currentBudget);
    }

    suspend fun getCurrentBudget(): BudgetEntity {
        return budgetDao.getCurrentBudget();
    }

    suspend fun getBudget(budgetId: Long): BudgetEntity {
        return budgetDao.getBudget(budgetId)
    }

    suspend fun countBudgets(): Int {
        return budgetDao.countBudgets()
    }

    suspend fun incrementBudgetedAmount(budgetId: Long, incrementBy: Long) {
        val curBudget = budgetDao.getBudget(budgetId)
        curBudget.budgetedAmount += incrementBy
        upsertBudget(curBudget)
    }

    suspend fun decrementBudgetedAmount(budgetId: Long, decrementBy: Long) {
        val curBudget = budgetDao.getBudget(budgetId)
        curBudget.budgetedAmount -= decrementBy
        upsertBudget(curBudget)
    }

    suspend fun incrementSpentAmount(budgetId: Long, incrementBy: Long) {
        val curBudget = budgetDao.getBudget(budgetId)
        curBudget.spentAmount += incrementBy
        upsertBudget(curBudget)
    }

    suspend fun activateBudget(budgetId: Long) {
        budgetDao.activateBudget(budgetId)
    }

    fun searchBudgetsByName(search: String): Flow<List<BudgetEntity>> {
        println("search: $search")
        return budgetDao.searchBudgetsByName("%${search}%")
    }

    suspend fun getBudgetByName(name: String): BudgetEntity? {
        return budgetDao.getBudgetByName(name)
    }
}