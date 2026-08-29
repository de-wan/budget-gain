package co.ke.foxlysoft.budgetgain.database

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CommonCategoryCatalogTest {
    @Test
    fun catalogContainsUniqueStableKeysAndMaterialColors() {
        assertEquals(9, CommonCategoryCatalog.categories.size)
        assertEquals(
            CommonCategoryCatalog.categories.size,
            CommonCategoryCatalog.categories.map { it.key }.toSet().size,
        )
        CommonCategoryCatalog.categories.forEach { category ->
            assertTrue(category.iconKey.isNotBlank())
            assertTrue(category.lightColorArgb ushr 24 == 0xFFL)
            assertTrue(category.darkColorArgb ushr 24 == 0xFFL)
            assertEquals(
                category.subCategories.size,
                category.subCategories.map { it.key }.toSet().size,
            )
        }
        assertNotNull(CommonCategoryCatalog.find("transport")?.subCategories?.find { it.key == "fuel" })
    }

    @Test
    fun missingPresentationValuesInheritFromParent() {
        val parent = CategoryEntity(
            budgetId = 1,
            name = "Mobile",
            amount = 0,
            spentAmount = 0,
            iconKey = "smartphone",
            lightColorArgb = 0xFF0097A7,
            darkColorArgb = 0xFF4DD0E1,
        )
        val child = SubCategoryEntity(categoryId = 1, name = "Airtime")

        assertEquals("smartphone", child.resolvedIconKey(parent))
        assertEquals(parent.lightColorArgb, child.resolvedColorArgb(parent, false))
        assertEquals(parent.darkColorArgb, child.resolvedColorArgb(parent, true))
    }
}
