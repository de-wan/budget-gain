package co.ke.foxlysoft.budgetgain.repos

import co.ke.foxlysoft.budgetgain.database.AppDatabase
import co.ke.foxlysoft.budgetgain.database.TransactionEntity
import kotlinx.coroutines.flow.first
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class TransactionRepository(db: AppDatabase) {
    private val transactionDao = db.transactionDao()
    private val accountDao = db.accountDao()

    suspend fun upsertTransaction(transactionEntity: TransactionEntity) : Long = transactionDao.upsert(transactionEntity)

    suspend fun deleteTransaction(transactionEntity: TransactionEntity) = transactionDao.delete(transactionEntity)

    fun getCategoryTransactions(categoryId: Long) = transactionDao.getCategoryTransactions(categoryId)

    suspend fun getPagingCategoryTransactions(categoryId: Long, limit: Int, offset: Int) = transactionDao.getPagingCategoryTransactions(categoryId, limit, offset)

    suspend fun getPagingMerchantTransactions(budgetId: Long, accountId: Long, limit: Int, offset: Int) = transactionDao.getPagingMerchantTransactions(budgetId, accountId, limit, offset)

    suspend fun existsByRef(ref: String) = transactionDao.existsByRef(ref)

    suspend fun getByRef(ref: String) = transactionDao.getByRef(ref)

    private data class MerchantInfo(
        var spentAmount: Long,
        var transactionCount: Int
    )

    suspend fun getMerchantSummaryForCategory(categoryId: Long): List<MerchantSummary> {
        val categoryTransactions = transactionDao.getCategoryTransactions(categoryId).first()
        val merchantMap = mutableMapOf<String, MerchantInfo>()
        val merchantNames = mutableMapOf<Long, String>()



        for (transaction in categoryTransactions) {
            val merchantName = merchantNames.getOrPut(transaction.creditAccountId, {
                accountDao.getAccount(transaction.creditAccountId).name
            })
            if (merchantMap.containsKey(merchantName)) {
                merchantMap[merchantName]!!.spentAmount += transaction.amount
                merchantMap[merchantName]!!.transactionCount++
            } else {
                merchantMap[merchantName] = MerchantInfo(transaction.amount, 1)
            }
        }

        return merchantMap.map { (name, info) ->
            MerchantSummary(merchantName = name, spentAmount = info.spentAmount, transactionCount = info.transactionCount)
        }.sortedByDescending { it.spentAmount }
    }

    suspend fun getCurrentMonthDailySpendByCategory(categoryId: Long): List<Pair<Int, Long>> {
        val categoryTransactions = transactionDao.getCategoryTransactions(categoryId).first()

        val dailySpendings = mutableMapOf<Int, Long>()

        for (transaction in categoryTransactions) {
            val datePart = transaction.timestamp.takeIf { it.length >= 10 }?.substring(0, 10) ?: continue
            val localDate = runCatching { LocalDate.parse(datePart) }.getOrNull() ?: continue
            dailySpendings[localDate.day] =
                (dailySpendings[localDate.day] ?: 0L) + transaction.amount
        }

        return dailySpendings
            .toList()
            .sortedBy { it.first }
    }

    suspend fun getCurrentMonthDailySpend(budgetId: Long): List<Pair<Int, Long>> {
        val budgetTransactions = transactionDao.getBudgetTransactions(budgetId).first()

        val dailySpendings = mutableMapOf<Int, Long>()

        for (transaction in budgetTransactions) {
            val datePart = transaction.timestamp.takeIf { it.length >= 10 }?.substring(0, 10) ?: continue
            val localDate = runCatching { LocalDate.parse(datePart) }.getOrNull() ?: continue
            dailySpendings[localDate.day] =
                (dailySpendings[localDate.day] ?: 0L) + transaction.amount
        }

        return dailySpendings
            .toList()
            .sortedBy { it.first }
    }
}
