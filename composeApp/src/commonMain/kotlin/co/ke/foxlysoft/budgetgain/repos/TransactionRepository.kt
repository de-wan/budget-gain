package co.ke.foxlysoft.budgetgain.repos

import co.ke.foxlysoft.budgetgain.database.AppDatabase
import co.ke.foxlysoft.budgetgain.database.TransactionEntity

class TransactionRepository(db: AppDatabase) {
    private val transactionDao = db.transactionDao()

    suspend fun upsertTransaction(transactionEntity: TransactionEntity) = transactionDao.upsert(transactionEntity)

    suspend fun deleteTransaction(transactionEntity: TransactionEntity) = transactionDao.delete(transactionEntity)

    fun getCategoryTransactions(categoryId: Long) = transactionDao.getCategoryTransactions(categoryId)

    suspend fun getPagingCategoryTransactions(categoryId: Long, limit: Int, offset: Int) = transactionDao.getPagingCategoryTransactions(categoryId, limit, offset)
}