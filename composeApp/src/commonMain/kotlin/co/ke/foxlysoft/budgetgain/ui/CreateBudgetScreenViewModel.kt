package co.ke.foxlysoft.budgetgain.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ke.foxlysoft.budgetgain.database.BudgetEntity
import co.ke.foxlysoft.budgetgain.repos.BudgetRepository
import kotlinx.coroutines.launch

class CreateBudgetScreenViewModel(
    private val budgetRepository: BudgetRepository
) : ViewModel() {
    fun createBudget(budgetEntity: BudgetEntity) {
        viewModelScope.launch {
            val budgetsCount = budgetRepository.countBudgets()
            if (budgetsCount == 0) {
                budgetEntity.isActive = true
            }
            budgetRepository.upsertBudget(budgetEntity)
        }
    }
}