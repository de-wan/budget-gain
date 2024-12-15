package co.ke.foxlysoft.budgetgain.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ke.foxlysoft.budgetgain.database.AccountEntity
import co.ke.foxlysoft.budgetgain.database.CategoryEntity
import co.ke.foxlysoft.budgetgain.database.TransactionEntity
import co.ke.foxlysoft.budgetgain.repos.AccountRepository
import co.ke.foxlysoft.budgetgain.repos.CategoryRepository
import co.ke.foxlysoft.budgetgain.repos.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CategoryDetailsScreenViewModel(
    private val categoryId: Long,
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
): ViewModel() {
    val currentCategory: StateFlow<CategoryEntity> = categoryRepository.getCategory(categoryId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CategoryEntity(
                id = 0L,
                budgetId = 0L,
                name = "",
                amount = 0L,
                spentAmount = 0L,
                createdAt = 0L,
            )
        )

    fun getCategoryTransactions(categoryId: Long): StateFlow<List<TransactionEntity>> {
        return transactionRepository.getCategoryTransactions(categoryId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList() // Default initial value
            )
    }

    fun getMerchantAccount(transaction: TransactionEntity, onComplete: (AccountEntity) -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val acc = accountRepository.getAccount(transaction.creditAccountId)
                onComplete(acc)
            }
        }
    }
}