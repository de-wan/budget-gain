package co.ke.foxlysoft.budgetgain.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CategorySelectionStateTest {
    @Test
    fun selectionCanChangeAndBeCleared() {
        val selection = CategorySelectionState(10L)

        selection.select(20L)
        assertEquals(20L, selection.categoryId.value)

        selection.clear()
        assertNull(selection.categoryId.value)
    }
}
