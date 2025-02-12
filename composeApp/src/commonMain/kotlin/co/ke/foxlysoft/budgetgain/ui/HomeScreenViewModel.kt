package co.ke.foxlysoft.budgetgain.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ke.foxlysoft.budgetgain.database.BudgetEntity
import co.ke.foxlysoft.budgetgain.database.CategoryEntity
import co.ke.foxlysoft.budgetgain.database.MpesaSmsEntity
import co.ke.foxlysoft.budgetgain.database.SettingsEntity
import co.ke.foxlysoft.budgetgain.repos.BudgetRepository
import co.ke.foxlysoft.budgetgain.repos.CategoryRepository
import co.ke.foxlysoft.budgetgain.repos.MpesaSmsRepository
import co.ke.foxlysoft.budgetgain.repos.SettingsRepository
import co.ke.foxlysoft.budgetgain.utils.QueryState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeScreenViewModel(
    private val settingsRepository: SettingsRepository,
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository,
    private val mpesaSmsRepository: MpesaSmsRepository
) : ViewModel() {
    private val _pageState =
        MutableStateFlow<QueryState>(QueryState.LOADING)
    val pageState: StateFlow<QueryState>
        get() = _pageState.asStateFlow()

    val firstTime: StateFlow<SettingsEntity> = settingsRepository.getSetting("firstTime")
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsEntity()
        )

    private val _currentBudget =
        MutableStateFlow<BudgetEntity>(BudgetEntity())
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


    // Dynamically filter categories based on the passed budgetId
    fun getBudgetCategories(budgetId: Long): StateFlow<List<CategoryEntity>> {
        return categoryRepository.getBudgetCategoriesFlow(budgetId)
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