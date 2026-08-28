package co.ke.foxlysoft.budgetgain.ui

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import co.ke.foxlysoft.budgetgain.database.AccountEntity
import co.ke.foxlysoft.budgetgain.database.AppDatabase
import co.ke.foxlysoft.budgetgain.database.BudgetEntity
import co.ke.foxlysoft.budgetgain.database.CategoryEntity
import co.ke.foxlysoft.budgetgain.database.MpesaSmsEntity
import co.ke.foxlysoft.budgetgain.database.TransactionEntity
import co.ke.foxlysoft.budgetgain.repos.AccountRepository
import co.ke.foxlysoft.budgetgain.repos.BudgetRepository
import co.ke.foxlysoft.budgetgain.repos.CategoryRepository
import co.ke.foxlysoft.budgetgain.repos.MpesaSmsRepository
import co.ke.foxlysoft.budgetgain.repos.TransactionRepository
import co.ke.foxlysoft.budgetgain.utils.MpesaSmsTypes
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TransactionRestorationIntegrationTest {
    private lateinit var database: AppDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder<AppDatabase>(context).build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun deleteTransactionRestoresBalancesAndLinkedSms() {
        runBlocking {
            val budget = BudgetEntity(id = 1, yearMonth = "2024-02", spentAmount = 2_500)
        val category = CategoryEntity(
            id = 1,
            budgetId = budget.id,
            name = "Food",
            amount = 10_000,
            spentAmount = 2_500,
        )
        val budgetAccount = AccountEntity(id = 1, balance = 7_500)
        val merchantAccount = AccountEntity(id = 2, balance = 2_500)
        val transaction = TransactionEntity(
            id = 1,
            ref = "RESTORE-1",
            budgetId = budget.id,
            categoryId = category.id,
            debitAccountId = budgetAccount.id,
            creditAccountId = merchantAccount.id,
            amount = 2_500,
        )
        val sms = MpesaSmsEntity(
            id = 1,
            transactionId = transaction.id,
            smsType = MpesaSmsTypes.TILL,
            ref = transaction.ref,
            amount = transaction.amount,
            dateTime = 0,
            subjectPrimaryIdentifierType = "name",
            subjectPrimaryIdentifier = "Merchant",
            subjectSecondaryIdentifierType = "",
            subjectSecondaryIdentifier = "",
            cost = 0,
            balance = 0,
        )

        database.budgetDao().upsert(budget)
        database.categoryDao().upsert(category)
        database.accountDao().upsert(budgetAccount)
        database.accountDao().upsert(merchantAccount)
        database.transactionDao().upsert(transaction)
        database.mpesaSmsDao().upsert(sms)

        val viewModel = CategoryDetailsScreenViewModel(
            categoryId = category.id,
            categoryRepository = CategoryRepository(database),
            transactionRepository = TransactionRepository(database),
            mpesaSmsRepository = MpesaSmsRepository(database),
            accountRepository = AccountRepository(database),
            budgetRepository = BudgetRepository(database),
        )

        viewModel.deleteTransaction(transaction)

        assertEquals(0L, database.accountDao().getAccount(merchantAccount.id).balance)
        assertEquals(10_000L, database.accountDao().getAccount(budgetAccount.id).balance)
        assertEquals(0L, database.categoryDao().getCategory(category.id).spentAmount)
        assertEquals(0L, database.budgetDao().getBudget(budget.id).spentAmount)
        assertEquals(0L, database.mpesaSmsDao().getMpesaSmsById(listOf(sms.id)).single().transactionId)
            assertTrue(database.transactionDao().getCategoryTransactions(category.id).first().isEmpty())
        }
    }
}
