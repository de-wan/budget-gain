package co.ke.foxlysoft.budgetgain.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ke.foxlysoft.budgetgain.database.BudgetEntity
import co.ke.foxlysoft.budgetgain.database.CategoryEntity
import co.ke.foxlysoft.budgetgain.database.CommonCategoryCatalog
import co.ke.foxlysoft.budgetgain.database.CommonCategoryDefinition
import co.ke.foxlysoft.budgetgain.database.DEFAULT_CATEGORY_DARK_COLOR_ARGB
import co.ke.foxlysoft.budgetgain.database.DEFAULT_CATEGORY_ICON_KEY
import co.ke.foxlysoft.budgetgain.database.DEFAULT_CATEGORY_LIGHT_COLOR_ARGB
import co.ke.foxlysoft.budgetgain.database.SubCategoryEntity
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
    val commonCategories = CommonCategoryCatalog.categories
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
    fun getBudgetCategoriesFlow(budgetId: Long) = categoryRepository.getBudgetCategoriesFlow(budgetId)

    fun createCategory(
        categoryEntity: CategoryEntity,
        commonDefinition: CommonCategoryDefinition? = null,
        customSubCategoryNames: List<String> = emptyList(),
        onComplete: () -> Unit = {},
    ) {
        viewModelScope.launch {
            val categoryId = if (commonDefinition == null) {
                categoryRepository.upsertCategory(categoryEntity)
            } else {
                categoryRepository.upsertCategory(
                    categoryEntity.copy(
                        catalogKey = CommonCategoryCatalog.normalizedKey(categoryEntity.name),
                        lightColorArgb = commonDefinition.lightColorArgb,
                        darkColorArgb = commonDefinition.darkColorArgb,
                        iconKey = commonDefinition.iconKey,
                    )
                )
            }
            val existingNames = categoryRepository.getSubCategories(categoryId)
                .map { it.name.lowercase() }.toMutableSet()
            if (commonDefinition != null) {
                commonDefinition.subCategories
                    .map { child ->
                        SubCategoryEntity(
                            categoryId = categoryId,
                            catalogKey = "${CommonCategoryCatalog.normalizedKey(categoryEntity.name)}.${child.key}",
                            name = child.name,
                            iconKey = child.iconKey,
                            lightColorArgb = child.lightColorArgb ?: commonDefinition.lightColorArgb,
                            darkColorArgb = child.darkColorArgb ?: commonDefinition.darkColorArgb,
                        )
                    }
                    .forEach { subCategory ->
                        if (subCategory.name.lowercase() !in existingNames) {
                            categoryRepository.upsertSubCategory(subCategory)
                            existingNames += subCategory.name.lowercase()
                        }
                    }
            }
            customSubCategoryNames
                .map { it.trim() }
                .filter { it.isNotEmpty() && it.lowercase() !in existingNames }
                .distinctBy { it.lowercase() }
                .forEach { name ->
                    categoryRepository.upsertSubCategory(
                        SubCategoryEntity(
                            categoryId = categoryId,
                            catalogKey = CommonCategoryCatalog.normalizedKey(name),
                            name = name
                        )
                    )
                }
            // update budgeted amount of budget
            budgetRepository.incrementBudgetedAmount(categoryEntity.budgetId, categoryEntity.amount)
            onComplete()
        }
    }
}
