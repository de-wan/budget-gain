package co.ke.foxlysoft.budgetgain.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Transaction
import co.ke.foxlysoft.budgetgain.database.CategoryEntity
import co.ke.foxlysoft.budgetgain.repos.BudgetRepository
import co.ke.foxlysoft.budgetgain.repos.CategoryRepository
import kotlinx.coroutines.launch

class EditCategoryScreenViewModel(
    private val categoryRepository: CategoryRepository,
    private val budgetRepository: BudgetRepository
): ViewModel() {
    @Transaction
    fun editCategory(categoryEntity: CategoryEntity){
        viewModelScope.launch {
            val oldCategory = categoryRepository.getCategory(categoryEntity.id)

            val incrementAmountBy = categoryEntity.amount - oldCategory.amount
            categoryRepository.upsertCategory(categoryEntity)

            // update budgeted amount of budget
            budgetRepository.incrementBudgetedAmount(categoryEntity.budgetId, incrementAmountBy)
        }
    }
}