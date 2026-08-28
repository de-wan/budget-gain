package co.ke.foxlysoft.budgetgain.database

import android.content.Context
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseMigrationTest {
    @get:Rule
    val migrationHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
    )

    private val context: Context = ApplicationProvider.getApplicationContext()

    @After
    fun cleanUp() {
        context.deleteDatabase(TEST_DATABASE)
    }

    @Test
    fun migrate14To18PreservesBudgetAndSmsData() {
        runBlocking {
            migrationHelper.createDatabase(TEST_DATABASE, 14).apply {
            execSQL(
                """
                INSERT INTO BudgetEntity (
                    id, name, isActive, initialBalance, budgetedAmount,
                    spentAmount, startDate, endDate, createdAt
                ) VALUES (1, 'February', 1, 100000, 50000, 10000,
                    1706745600000, 1709251199999, 1706745600)
                """.trimIndent()
            )
            execSQL(
                """
                INSERT INTO MpesaSmsEntity (
                    id, transactionId, smsType, ref, amount, dateTime,
                    subjectPrimaryIdentifierType, subjectPrimaryIdentifier,
                    subjectSecondaryIdentifierType, subjectSecondaryIdentifier,
                    cost, balance
                ) VALUES (1, 0, 'TILL', 'TESTREF', 2500, 1707000000000,
                    'name', 'Test Merchant', '', '', 0, 97500)
                """.trimIndent()
            )
            close()
        }

            val database = Room.databaseBuilder<AppDatabase>(context, TEST_DATABASE)
                .addMigrations(*APP_DATABASE_MIGRATIONS)
                .openHelperFactory(FrameworkSQLiteOpenHelperFactory())
                .build()

            try {
                val budget = database.budgetDao().getBudget(1)
                val sms = database.mpesaSmsDao().getMpesaSmsById(listOf(1)).single()

                assertEquals("2024-02", budget.yearMonth)
                assertEquals(100_000L, budget.initialBalance)
                assertEquals("TESTREF", sms.ref)
                assertFalse(sms.isIgnored)
            } finally {
                database.close()
            }
        }
    }

    private companion object {
        const val TEST_DATABASE = "migration-test"
    }
}
