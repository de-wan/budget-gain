package co.ke.foxlysoft.budgetgain.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ke.foxlysoft.budgetgain.database.BudgetEntity
import co.ke.foxlysoft.budgetgain.database.CategoryEntity
import co.ke.foxlysoft.budgetgain.repos.BudgetRepository
import co.ke.foxlysoft.budgetgain.repos.CategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AddCategoryScreenViewModel (
    private val categoryRepository: CategoryRepository,
    private val budgetRepository: BudgetRepository
): ViewModel(){
    private val _currentBudget =
        MutableStateFlow(BudgetEntity())
    val currentBudget: StateFlow<BudgetEntity>
        get() = _currentBudget.asStateFlow()

    init {
        budgetRepository.getCurrentBudget(
            onStarted = {},
            onComplete = { currentBudgetFlow ->
                viewModelScope.launch {
                    currentBudgetFlow.collect { currentBudget ->
                        if (currentBudget != null) {
                            _currentBudget.value = currentBudget
                        }
                    }
                }
            }
        )
    }
    fun createCategory(categoryEntity: CategoryEntity){
        viewModelScope.launch {
            categoryRepository.upsertCategory(categoryEntity)
            // update budgeted amount of budget
            budgetRepository.incrementBudgetedAmount(categoryEntity.budgetId, categoryEntity.amount)
        }
    }
}