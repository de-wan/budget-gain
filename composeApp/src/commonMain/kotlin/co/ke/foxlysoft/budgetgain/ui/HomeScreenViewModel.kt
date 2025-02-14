package co.ke.foxlysoft.budgetgain.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ke.foxlysoft.budgetgain.database.BudgetEntity
import co.ke.foxlysoft.budgetgain.database.CategoryEntity
import co.ke.foxlysoft.budgetgain.database.MpesaSmsEntity
import co.ke.foxlysoft.budgetgain.repos.BudgetRepository
import co.ke.foxlysoft.budgetgain.repos.CategoryRepository
import co.ke.foxlysoft.budgetgain.repos.MpesaSmsRepository
import co.ke.foxlysoft.budgetgain.repos.SettingsRepository
import co.ke.foxlysoft.budgetgain.utils.QueryState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeScreenViewModel(
    private val settingsRepository: SettingsRepository,
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository,
    private val mpesaSmsRepository: MpesaSmsRepository
) : ViewModel() {
    private val _pageState =
        MutableStateFlow(QueryState.LOADING)
    val pageState: StateFlow<QueryState>
        get() = _pageState.asStateFlow()

    private val _currentBudget =
        MutableStateFlow(BudgetEntity())
    val currentBudget: StateFlow<BudgetEntity>
        get() = _currentBudget.asStateFlow()

    suspend fun upsertMpesaSms(mpesaSmsEntity: MpesaSmsEntity) {
        mpesaSmsRepository.upsertMpesaSms(mpesaSmsEntity)
    }

    init {
        budgetRepository.getCurrentBudget(
            onStarted = {
                _pageState.value = QueryState.LOADING
            },
            onComplete = { currentBudgetFlow ->
                viewModelScope.launch {
                    currentBudgetFlow.collect { currentBudget ->
                        if (currentBudget == null) {
                            _pageState.value = QueryState.NO_RESULTS
                        } else {
                            _currentBudget.value = currentBudget
                            _pageState.value = QueryState.COMPLETE
                        }
                    }
                }

            }
        )
    }


    suspend fun getBudgetCategories(limit : Int, offset : Int): List<CategoryEntity> {
        val budgetId = currentBudget.value.id
        if (budgetId == 0L) {
            return emptyList()
        }

        return categoryRepository.getPagingBudgetCategories(budgetId,
            limit, offset
        )
    }

    suspend fun deleteCategory(category: CategoryEntity) {
        categoryRepository.deleteCategory(category)
        budgetRepository.decrementBudgetedAmount(category.budgetId, category.amount)
    }

    init {
        viewModelScope.launch {
            settingsRepository.setSetting("firstTime", "true")
        }
    }
}