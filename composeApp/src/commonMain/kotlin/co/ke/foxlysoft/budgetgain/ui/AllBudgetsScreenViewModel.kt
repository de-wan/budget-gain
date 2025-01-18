package co.ke.foxlysoft.budgetgain.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ke.foxlysoft.budgetgain.database.BudgetEntity
import co.ke.foxlysoft.budgetgain.repos.BudgetRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AllBudgetsScreenViewModel(
    private val budgetRepository: BudgetRepository
): ViewModel() {
    val allBudgets: StateFlow<List<BudgetEntity>> = budgetRepository.getAllBudgets()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun deleteBudget(budgetEntity: BudgetEntity) {
        viewModelScope.launch {
            budgetRepository.deleteBudget(budgetEntity)
        }
    }

    fun activateBudget(budgetId: Long) {
        viewModelScope.launch {
            budgetRepository.activateBudget(budgetId)
        }
    }
}