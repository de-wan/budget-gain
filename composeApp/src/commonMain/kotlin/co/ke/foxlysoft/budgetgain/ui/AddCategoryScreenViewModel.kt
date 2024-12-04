package co.ke.foxlysoft.budgetgain.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ke.foxlysoft.budgetgain.database.CategoryEntity
import co.ke.foxlysoft.budgetgain.repos.BudgetRepository
import co.ke.foxlysoft.budgetgain.repos.CategoryRepository
import kotlinx.coroutines.launch

class AddCategoryScreenViewModel (
    private val categoryRepository: CategoryRepository,
    private val budgetRepository: BudgetRepository
): ViewModel(){
    fun createCategory(categoryEntity: CategoryEntity){
        viewModelScope.launch {
            categoryRepository.upsertCategory(categoryEntity)
            // update budgeted amount of budget
            budgetRepository.incrementBudgetedAmount(categoryEntity.budgetId, categoryEntity.amount)
        }
    }
}