package co.ke.foxlysoft.budgetgain.ui

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal class CategorySelectionState(initialCategoryId: Long?) {
    private val mutableCategoryId = MutableStateFlow(initialCategoryId)
    val categoryId: StateFlow<Long?> = mutableCategoryId

    fun select(categoryId: Long) {
        mutableCategoryId.value = categoryId
    }

    fun clear() {
        mutableCategoryId.value = null
    }
}
