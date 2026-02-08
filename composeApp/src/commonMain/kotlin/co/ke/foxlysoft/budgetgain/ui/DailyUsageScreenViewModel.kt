package co.ke.foxlysoft.budgetgain.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ke.foxlysoft.budgetgain.database.BudgetEntity
import co.ke.foxlysoft.budgetgain.repos.BudgetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DailyUsageScreenViewModel (
    private val budgetRepository: BudgetRepository
): ViewModel() {
    private val _currentBudget =
        MutableStateFlow(BudgetEntity())
    val currentBudget: StateFlow<BudgetEntity>
        get() = _currentBudget.asStateFlow()

    private val _month = MutableStateFlow(0)
    val month: StateFlow<Int>
        get() = _month.asStateFlow()

    private val _year = MutableStateFlow(0)
    val year: StateFlow<Int>
        get() = _year.asStateFlow()

    init {
        budgetRepository.getCurrentBudget(
            onStarted = {},
            onComplete = { currentBudgetFlow ->
                viewModelScope.launch {
                    currentBudgetFlow.collect { currentBudget ->
                        if (currentBudget != null) {
                            _currentBudget.value = currentBudget
                            val splitMonthYear = currentBudget.yearMonth.split("-").map { it.toInt() }
                            _month.value = splitMonthYear[1]
                            _year.value = splitMonthYear[0]
                        }
                    }
                }
            }
        )
    }
}