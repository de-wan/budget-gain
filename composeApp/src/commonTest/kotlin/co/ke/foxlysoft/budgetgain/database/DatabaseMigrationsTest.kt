package co.ke.foxlysoft.budgetgain.database

import kotlin.test.Test
import kotlin.test.assertEquals

class DatabaseMigrationsTest {
    @Test
    fun migrationPathCoversEveryPersistedSchemaThroughCurrentVersion() {
        assertEquals(
            listOf(14 to 18, 15 to 18, 16 to 18, 18 to 19),
            APP_DATABASE_MIGRATIONS.map { it.startVersion to it.endVersion },
        )
    }
}
