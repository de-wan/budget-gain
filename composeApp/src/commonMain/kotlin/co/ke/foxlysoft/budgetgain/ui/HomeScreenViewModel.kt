package co.ke.foxlysoft.budgetgain.ui

import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ke.foxlysoft.budgetgain.database.BudgetEntity
import co.ke.foxlysoft.budgetgain.database.CategoryEntity
import co.ke.foxlysoft.budgetgain.database.SettingsEntity
import co.ke.foxlysoft.budgetgain.repos.BudgetRepository
import co.ke.foxlysoft.budgetgain.repos.CategoryRepository
import co.ke.foxlysoft.budgetgain.repos.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeScreenViewModel(
private val settingsRepository: SettingsRepository,
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository
): ViewModel() {

    val firstTime: StateFlow<SettingsEntity> = settingsRepository.getSetting("firstTime")
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsEntity()
        )

    val currentBudget: StateFlow<BudgetEntity> = budgetRepository.getCurrentBudget()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = BudgetEntity()
        )

    // Dynamically filter categories based on the passed budgetId
    fun getBudgetCategories(budgetId: Long): StateFlow<List<CategoryEntity>> {
        return categoryRepository.getBudgetCategories(budgetId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList() // Default initial value
            )
    }

    suspend fun deleteCategory(category: CategoryEntity) {
        categoryRepository.deleteCategory(category)
        budgetRepository.decrementBudgetedAmount(category.budgetId, category.amount)
    }

    suspend fun setToFalse() {
        settingsRepository.setSetting("firstTime", "false")
    }

    init {
        viewModelScope.launch {
            settingsRepository.setSetting("firstTime", "true")
        }
    }
}