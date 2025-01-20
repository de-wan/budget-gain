package co.ke.foxlysoft.budgetgain.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Transaction
import co.ke.foxlysoft.budgetgain.database.BudgetEntity
import co.ke.foxlysoft.budgetgain.database.CategoryEntity
import co.ke.foxlysoft.budgetgain.repos.BudgetRepository
import co.ke.foxlysoft.budgetgain.repos.CategoryRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CreateBudgetScreenViewModel(
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _selectableBudgets = MutableStateFlow<List<BudgetEntity>>(emptyList())
    val selectableBudgets: StateFlow<List<BudgetEntity>> = _selectableBudgets

    private var _searchJob: Job? = null
    // Function to update the search query
    fun updateBudgetSearchQuery(query: String) {
        _searchJob?.cancel()
        _searchJob = viewModelScope.launch {
            if (query.isNotEmpty()) {
                delay(500)
                budgetRepository.searchBudgetsByName(query).collectLatest {
                    _selectableBudgets.value = it
                }
            } else {
                _selectableBudgets.value = emptyList()
            }
        }
    }

    @Transaction
    fun createBudget(budgetEntity: BudgetEntity, copyCategoriesFrom: String) {
        viewModelScope.launch {
            val budgetsCount = budgetRepository.countBudgets()
            if (budgetsCount == 0) {
                budgetEntity.isActive = true
            }

            val budgetId =budgetRepository.upsertBudget(budgetEntity)
            budgetEntity.id = budgetId

            // copy categories from budget
            val budgetToCopyCategoriesFrom = budgetRepository.getBudgetByName(copyCategoriesFrom)
            if (budgetToCopyCategoriesFrom != null) {
                budgetEntity.budgetedAmount = budgetToCopyCategoriesFrom.budgetedAmount

                // copy categories
                val categories = categoryRepository.getBudgetCategories(budgetToCopyCategoriesFrom.id)
                categories.forEach {
                    val categoryEntity = CategoryEntity(
                        budgetId = budgetEntity.id,
                        name = it.name,
                        amount = it.amount,
                        spentAmount = 0L,
                    )
                    categoryRepository.upsertCategory(categoryEntity)
                }

                budgetRepository.upsertBudget(budgetEntity)
            }


        }
    }


}