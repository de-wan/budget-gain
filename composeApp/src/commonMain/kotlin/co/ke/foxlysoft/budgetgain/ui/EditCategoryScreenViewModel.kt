package co.ke.foxlysoft.budgetgain.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Transaction
import co.ke.foxlysoft.budgetgain.database.BudgetEntity
import co.ke.foxlysoft.budgetgain.database.CategoryEntity
import co.ke.foxlysoft.budgetgain.repos.BudgetRepository
import co.ke.foxlysoft.budgetgain.repos.CategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EditCategoryScreenViewModel(
    private val categoryRepository: CategoryRepository,
    private val budgetRepository: BudgetRepository
): ViewModel() {
    private val _currentBudget =
        MutableStateFlow(BudgetEntity())
    val currentBudget: StateFlow<BudgetEntity>
        get() = _currentBudget.asStateFlow()

    init {
        budgetRepository.getCurrentBudget({}, { currentBudgetFlow ->
            viewModelScope.launch {
                currentBudgetFlow.collect { currentBudget ->
                    if (currentBudget == null) {
                        _currentBudget.value = BudgetEntity()
                    } else {
                        _currentBudget.value = currentBudget
                    }
                }
            }
        })
    }

    suspend fun getCategory(categoryId: Long): CategoryEntity {
        return categoryRepository.getCategory(categoryId)
    }

    @Transaction
    fun editCategory(categoryEntity: CategoryEntity, onComplete: () -> Unit){
        viewModelScope.launch {
            val oldCategory = categoryRepository.getCategory(categoryEntity.id)

            val incrementAmountBy = categoryEntity.amount - oldCategory.amount
            categoryRepository.upsertCategory(categoryEntity)

            // update budgeted amount of budget
            budgetRepository.incrementBudgetedAmount(categoryEntity.budgetId, incrementAmountBy)
            onComplete()
        }
    }
}