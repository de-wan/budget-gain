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

    fun existsByRef(ref: String) = transactionDao.existsByRef(ref)

    suspend fun getMerchantSummaryForCategory(categoryId: Long): List<MerchantSummary> {
        val categoryTransactions = transactionDao.getCategoryTransactions(categoryId).first()
        val merchantMap = mutableMapOf<String, Long>()

        for (transaction in categoryTransactions) {
            val merchantName = accountDao.getAccount(transaction.creditAccountId).name
            if (merchantMap.containsKey(merchantName)) {
                merchantMap[merchantName] = (merchantMap[merchantName] ?: 0) + transaction.amount
            } else {
                merchantMap[merchantName] = transaction.amount
            }
        }

        return merchantMap.map { (name, amount) ->
            MerchantSummary(merchantName = name, spentAmount = amount)
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
}